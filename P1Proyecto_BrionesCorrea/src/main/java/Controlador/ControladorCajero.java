package Controlador;

import Modelo.Venta;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ControladorCajero {

    public void cargarExcelEnTabla(String rutaArchivo, JTable tabla) {
        try (FileInputStream file = new FileInputStream(rutaArchivo); Workbook workbook = new XSSFWorkbook(file)) {

            Sheet hoja = workbook.getSheetAt(0);
            DefaultTableModel modelo = new DefaultTableModel();

            Row encabezado = hoja.getRow(0);
            for (Cell celda : encabezado) {
                modelo.addColumn(celda.getStringCellValue());
            }

            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila != null) {
                    Object[] datos = new Object[encabezado.getPhysicalNumberOfCells()];
                    for (int j = 0; j < datos.length; j++) {
                        Cell celda = fila.getCell(j);
                        datos[j] = (celda != null) ? celda.toString() : "";
                    }
                    modelo.addRow(datos);
                }
            }

            tabla.setModel(modelo);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null, "Error al leer el Excel: " + e.getMessage());
        }
    }

    public void agregarProductoAlCarrito(JTable tablaProductos, JTable tablaCarrito, javax.swing.JTextField txtCantidad) {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            javax.swing.JOptionPane.showMessageDialog(null, "Seleccione un producto primero.");
            return;
        }

        double stockDouble = Double.parseDouble(tablaProductos.getValueAt(filaSeleccionada, 2).toString());
        int stock = (int) stockDouble;

        double precio = Double.parseDouble(tablaProductos.getValueAt(filaSeleccionada, 3).toString());
        String codigo = tablaProductos.getValueAt(filaSeleccionada, 0).toString();
        String nombre = tablaProductos.getValueAt(filaSeleccionada, 1).toString();

        int cantidad;
        try {
            cantidad = Integer.parseInt(txtCantidad.getText());
        } catch (NumberFormatException e) {
            javax.swing.JOptionPane.showMessageDialog(null, "Ingrese una cantidad válida.");
            return;
        }

        if (cantidad <= 0 || cantidad > stock) {
            javax.swing.JOptionPane.showMessageDialog(null, "Cantidad inválida o mayor al stock disponible.");
            return;
        }

        double subtotal = precio * cantidad;

        DefaultTableModel modeloCarrito = (DefaultTableModel) tablaCarrito.getModel();
        modeloCarrito.addRow(new Object[]{codigo, nombre, cantidad, precio, subtotal});

        // Actualizar stock visualmente
        tablaProductos.setValueAt(stock - cantidad, filaSeleccionada, 2);

        // Limpiar campo cantidad
        txtCantidad.setText("");
    }

    public void calcularTotales(JTable tablaCarrito, javax.swing.JTextField txtSubtotal, javax.swing.JTextField txtDescuento, javax.swing.JTextField txtTotal) {
        double subtotal = 0.0;

        // Recorremos todas las filas del carrito
        for (int i = 0; i < tablaCarrito.getRowCount(); i++) {
            double subFila = Double.parseDouble(tablaCarrito.getValueAt(i, 4).toString());
            subtotal += subFila;
        }

        double descuento = 0.0;
        if (subtotal >= 50) {
            descuento = subtotal * 0.10; // 10% de descuento
        }

        double total = subtotal - descuento;

        // Mostramos resultados formateados
        txtSubtotal.setText(String.format("%.2f", subtotal));
        txtDescuento.setText(String.format("%.2f", descuento));
        txtTotal.setText(String.format("%.2f", total));
    }

    public void buscarProducto(JTable tablaProductos, String criterio) {
        DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tablaProductos.setRowSorter(sorter);

        // Si el texto está vacío, mostrar todos
        if (criterio.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        // Detectar si el criterio es número o texto
        RowFilter<DefaultTableModel, Object> filtro;
        if (criterio.matches("\\d+")) { // Solo números → buscar por código (columna 0)
            filtro = RowFilter.regexFilter("^" + criterio, 0);
        } else { // Texto → buscar por nombre (columna 1)
            filtro = RowFilter.regexFilter("(?i)" + criterio, 1);
        }

        sorter.setRowFilter(filtro);

        // 🔍 Mostrar mensaje si no hay resultados
        if (tablaProductos.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "⚠️ Producto no encontrado",
                    "Búsqueda",
                    javax.swing.JOptionPane.WARNING_MESSAGE
            );
            // Opcional: limpiar filtro después de mostrar el mensaje
            sorter.setRowFilter(null);
        }
    }

    public void limpiarBusqueda(JTable tablaProductos, javax.swing.JTextField txtBuscar) {
        DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tablaProductos.setRowSorter(sorter);

        // Quitar filtro
        sorter.setRowFilter(null);

        // Limpiar texto del campo
        txtBuscar.setText("");
    }
    // Método más simple

    public void registrarVenta(JTable tablaCarrito, ControladorProductos controladorProductos, String rutaExcelProductos) {
        try {
            DefaultTableModel modeloDetalle = (DefaultTableModel) tablaCarrito.getModel();

            if (modeloDetalle.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "⚠️ No hay productos en el carrito.");
                return;
            }

            Venta venta = new Venta();
            venta.setNumeroVenta(String.valueOf(System.currentTimeMillis()));
            venta.setHora(new SimpleDateFormat("HH:mm:ss").format(new Date()));

            StringBuilder detalle = new StringBuilder();
            double totalVenta = 0.0;

            // Recorrer carrito y actualizar stock
            for (int i = 0; i < modeloDetalle.getRowCount(); i++) {
                String codigo = modeloDetalle.getValueAt(i, 0).toString();
                String nombre = modeloDetalle.getValueAt(i, 1).toString();
                int cantidad = Integer.parseInt(modeloDetalle.getValueAt(i, 2).toString());
                double precio = Double.parseDouble(modeloDetalle.getValueAt(i, 3).toString());

                totalVenta += cantidad * precio;
                detalle.append(cantidad).append("x ").append(nombre);
                if (i < modeloDetalle.getRowCount() - 1) {
                    detalle.append(", ");
                }

                // Actualizar stock en listaProductos
                controladorProductos.actualizarStock(codigo, cantidad);
            }

            venta.setDetalleProductos(detalle.toString());
            venta.setValorVenta(totalVenta);

            // Guardar venta en Excel
            guardarVentaEnExcel(venta);

            // Guardar stock actualizado en Excel
            controladorProductos.guardarProductosEnExcel(controladorProductos.getListaProductos(), rutaExcelProductos);

            // Vaciar carrito
            modeloDetalle.setRowCount(0);

            JOptionPane.showMessageDialog(null, "✅ Venta registrada y stock actualizado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error al registrar la venta: " + e.getMessage());
        }
    }

    private void guardarVentaEnExcel(Venta venta) {
        try {
            String ruta = System.getProperty("user.home") + "/Desktop/ventas.xlsx";
            File archivo = new File(ruta);
            Workbook workbook;
            Sheet hoja;

            if (archivo.exists()) {
                FileInputStream fis = new FileInputStream(archivo);
                workbook = new XSSFWorkbook(fis);
                hoja = workbook.getSheetAt(0);
                fis.close();
            } else {
                workbook = new XSSFWorkbook();
                hoja = workbook.createSheet("Ventas");
                Row header = hoja.createRow(0);
                header.createCell(0).setCellValue("N° Venta");
                header.createCell(1).setCellValue("Hora");
                header.createCell(2).setCellValue("Detalle de Productos");
                header.createCell(3).setCellValue("Valor Venta");
            }

            int filaNueva = hoja.getLastRowNum() + 1;
            Row fila = hoja.createRow(filaNueva);
            fila.createCell(0).setCellValue(venta.getNumeroVenta());
            fila.createCell(1).setCellValue(venta.getHora());
            fila.createCell(2).setCellValue(venta.getDetalleProductos());
            fila.createCell(3).setCellValue(venta.getValorVenta());

            FileOutputStream fos = new FileOutputStream(archivo);
            workbook.write(fos);
            fos.close();
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error al guardar la venta: " + e.getMessage());
        }
    }

}
