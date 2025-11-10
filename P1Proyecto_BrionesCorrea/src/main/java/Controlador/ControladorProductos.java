package Controlador;

import Modelo.Producto;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ControladorProductos {

    private List<Producto> listaProductos = new ArrayList<>();

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public Producto buscarProductoPorCodigo(String codigo) {
        for (Producto p : listaProductos) {
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        return null;
    }

    public void actualizarStock(String codigo, int cantidadVendida) {
        for (Producto p : listaProductos) {
            if (p.getCodigo().equals(codigo)) {
                p.setStock(p.getStock() - cantidadVendida);
                break;
            }
        }
    }

    public void cargarExcelEnTablaProductos(String rutaArchivo, JTable tablaProductos) {
        try (FileInputStream file = new FileInputStream(new File(rutaArchivo)); Workbook workbook = new XSSFWorkbook(file)) {

            Sheet hoja = workbook.getSheetAt(0);
            DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
            modelo.setRowCount(0);
            listaProductos.clear();

            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila != null) {
                    // Leer todo como String
                    String codigoRaw = fila.getCell(0).toString().trim();
                    String codigoFormateado = String.format("%04d", Integer.parseInt(codigoRaw));
                    String nombre = fila.getCell(1).toString();
                    String stockStr = fila.getCell(2).toString();
                    String precioStr = fila.getCell(3).toString();
                    String caducidad = fila.getLastCellNum() > 4 && fila.getCell(4) != null
                            ? fila.getCell(4).toString() : "";

                    int stock = (int) Double.parseDouble(stockStr);
                    double precio = Double.parseDouble(precioStr);

                    modelo.addRow(new Object[]{codigoFormateado, nombre, stock, precio, caducidad});

                    Producto p = new Producto();
                    p.setCodigo(codigoFormateado);
                    p.setNombre(nombre);
                    p.setStock(stock);
                    p.setPrecio(precio);
                    p.setCaducidad(caducidad);
                    listaProductos.add(p);
                }
            }

            tablaProductos.setModel(modelo);

        } catch (Exception e) {
            e.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error al cargar el archivo Excel",
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public void buscarProducto(JTable tablaProductos, String criterio) {
        DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tablaProductos.setRowSorter(sorter);

        // Si el campo de búsqueda está vacío, mostrar todos
        if (criterio.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        // Detectar si el criterio es número o texto
        RowFilter<DefaultTableModel, Object> filtro;
        if (criterio.matches("\\d+")) { // Buscar por código (columna 0)
            filtro = RowFilter.regexFilter("^" + criterio, 0);
        } else { // Buscar por nombre (columna 1)
            filtro = RowFilter.regexFilter("(?i)" + criterio, 1);
        }

        sorter.setRowFilter(filtro);

        // Si no se encuentra nada
        if (tablaProductos.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Producto no encontrado",
                    "Búsqueda", javax.swing.JOptionPane.WARNING_MESSAGE);
        }
    }

    public void agregarProducto(JTextField txtCodigo, JTextField txtNombre,
            JTextField txtStock, JTextField txtPrecio, JTextField txtCaducidad,
            JTable tablaProductos, String rutaArchivo) {
        try {
            String codigo = txtCodigo.getText().trim();
            String nombre = txtNombre.getText().trim();
            String caducidadStr = txtCaducidad.getText().trim();

            if (codigo.isEmpty() || nombre.isEmpty() || caducidadStr.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Ingrese código, nombre y fecha de caducidad.");
                return;
            }

            // Validar formato de fecha
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate fechaCaducidad;
            try {
                fechaCaducidad = LocalDate.parse(caducidadStr, formatter);
            } catch (DateTimeParseException e) {
                JOptionPane.showMessageDialog(null, "Ingrese una fecha válida en formato yyyy-MM-dd.");
                return;
            }

            int stock = Integer.parseInt(txtStock.getText().trim());
            double precio = Double.parseDouble(txtPrecio.getText().trim());

            if (stock <= 0 || precio <= 0) {
                JOptionPane.showMessageDialog(null, "Stock y precio deben ser mayores a 0.");
                return;
            }

            // Verificar si el código ya existe
            for (Producto p : listaProductos) {
                if (p.getCodigo().equals(codigo)) {
                    JOptionPane.showMessageDialog(null, "El código ya existe.");
                    return;
                }
            }

            Producto nuevo = new Producto();
            nuevo.setCodigo(codigo);
            nuevo.setNombre(nombre);
            nuevo.setStock(stock);
            nuevo.setPrecio(precio);
            nuevo.setCaducidad(caducidadStr);

            listaProductos.add(nuevo);

            // Actualizar tabla
            DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
            modelo.addRow(new Object[]{codigo, nombre, stock, precio, caducidadStr});

            // Guardar en Excel
            guardarProductosEnExcel(listaProductos, rutaArchivo);

            // Limpiar campos
            txtCodigo.setText("");
            txtNombre.setText("");
            txtStock.setText("");
            txtPrecio.setText("");
            txtCaducidad.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Ingrese números válidos en stock y precio.");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al guardar producto.");
        }
    }

    public void guardarProductosEnExcel(List<Producto> lista, String rutaArchivo) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet hoja = workbook.createSheet("Productos");

        int filaNum = 0;
        Row fila = hoja.createRow(filaNum++);
        // Encabezados
        fila.createCell(0).setCellValue("Codigo");
        fila.createCell(1).setCellValue("Nombre");
        fila.createCell(2).setCellValue("Stock");
        fila.createCell(3).setCellValue("Precio");
        fila.createCell(4).setCellValue("Fecha de Caducidad");

        for (Producto p : lista) {
            fila = hoja.createRow(filaNum++);
            fila.createCell(0).setCellValue(p.getCodigo());
            fila.createCell(1).setCellValue(p.getNombre());
            fila.createCell(2).setCellValue(p.getStock());

            // Convertimos a double seguro
            double precio = 0.0;
            try {
                precio = Double.parseDouble(String.valueOf(p.getPrecio()));
            } catch (NumberFormatException e) {
                precio = 0.0; // en caso de error
            }
            fila.createCell(3).setCellValue(precio);

            fila.createCell(4).setCellValue(p.getCaducidad());
        }

        FileOutputStream fileOut = new FileOutputStream(rutaArchivo);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
    }

    public void eliminarProducto(JTable tablaProductos, String rutaExcel) {
        int filaSeleccionada = tablaProductos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un producto para eliminar.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
            String codigo = modelo.getValueAt(filaSeleccionada, 0).toString(); // asumiendo que columna 0 es código

            // Eliminar de la lista
            listaProductos.removeIf(p -> p.getCodigo().equals(codigo));

            // Eliminar de la tabla
            modelo.removeRow(filaSeleccionada);

            // Guardar cambios en Excel
            guardarProductosEnExcel(listaProductos, rutaExcel);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar el producto.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void eliminarTodosProductos(JTable tablaProductos, String rutaExcel) {
        int confirm = JOptionPane.showConfirmDialog(null,
                "¿Está seguro que desea eliminar TODOS los productos?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return; // usuario canceló
        }

        try {
            // Limpiar lista y tabla
            listaProductos.clear();
            DefaultTableModel modelo = (DefaultTableModel) tablaProductos.getModel();
            modelo.setRowCount(0);

            // Guardar cambios en Excel
            guardarProductosEnExcel(listaProductos, rutaExcel);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar los productos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void modificarProducto(JTable tabla, String rutaExcel) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un producto para modificar.");
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        String codigo = modelo.getValueAt(fila, 0).toString();

        try {
            String nombre = JOptionPane.showInputDialog("Nuevo nombre:", modelo.getValueAt(fila, 1));
            int stock = Integer.parseInt(JOptionPane.showInputDialog("Nuevo stock:", modelo.getValueAt(fila, 2)));
            double precio = Double.parseDouble(JOptionPane.showInputDialog("Nuevo precio:", modelo.getValueAt(fila, 3)));
            String cad = JOptionPane.showInputDialog("Nueva caducidad:", modelo.getValueAt(fila, 4));

            modelo.setValueAt(nombre, fila, 1);
            modelo.setValueAt(stock, fila, 2);
            modelo.setValueAt(precio, fila, 3);
            modelo.setValueAt(cad, fila, 4);

            for (Producto p : listaProductos) {
                if (p.getCodigo().equals(codigo)) {
                    p.setNombre(nombre);
                    p.setStock(stock);
                    p.setPrecio(precio);
                    p.setCaducidad(cad);
                    break;
                }
            }

            guardarProductosEnExcel(listaProductos, rutaExcel);
            JOptionPane.showMessageDialog(null, "Producto modificado correctamente.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al modificar producto.");
        }
    }

    public void cargarDesdeExcel(String rutaExcel, JTable tabla) {
        try (FileInputStream f = new FileInputStream(rutaExcel); Workbook wb = new XSSFWorkbook(f)) {

            Sheet hoja = wb.getSheetAt(0);
            DefaultTableModel m = (DefaultTableModel) tabla.getModel();
            m.setRowCount(0);
            listaProductos.clear();

            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row r = hoja.getRow(i);
                if (r == null) {
                    continue;
                }

                String c = r.getCell(0).toString();
                String n = r.getCell(1).toString();
                int s = (int) Double.parseDouble(r.getCell(2).toString());
                double p = Double.parseDouble(r.getCell(3).toString());
                String cad = r.getLastCellNum() > 4 && r.getCell(4) != null ? r.getCell(4).toString() : "";

                m.addRow(new Object[]{c, n, s, p, cad});

                // Crear Producto correctamente
                Producto producto = new Producto();
                producto.setCodigo(c);
                producto.setNombre(n);
                producto.setStock(s);
                producto.setPrecio(p);
                producto.setCaducidad(cad);
                listaProductos.add(producto);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar Excel");
            e.printStackTrace();
        }
    }

}
