package Controlador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import javax.swing.table.TableRowSorter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ControladorVentas {

    public void cargarVentasDesdeExcel(JTable tablaVentas) {
        try {
            String rutaUsuario = System.getProperty("user.home");
            File archivo = new File(rutaUsuario + "/Desktop/ventas.xlsx");

            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(null, "No hay registros de ventas todavía.");
                return;
            }

            FileInputStream fis = new FileInputStream(archivo);
            Workbook workbook = new XSSFWorkbook(fis);
            Sheet hoja = workbook.getSheetAt(0);

            DefaultTableModel modelo = (DefaultTableModel) tablaVentas.getModel();
            modelo.setRowCount(0); // limpiar tabla

            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila != null) {
                    Object[] datos = new Object[4];
                    for (int j = 0; j < 4; j++) {
                        datos[j] = fila.getCell(j) != null ? fila.getCell(j).toString() : "";
                    }
                    modelo.addRow(datos);
                }
            }

            workbook.close();
            fis.close();
            JOptionPane.showMessageDialog(null, "✅ Ventas cargadas correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "❌ Error al leer el archivo de ventas: " + e.getMessage());
        }
    }

    public void calcularTotalesVentas(JTable tablaVentas, JLabel lblTotalVentas, JLabel lblTotalProductos, JLabel lblTotalRecaudado) {
        int totalVentas = tablaVentas.getRowCount();
        int totalProductos = 0;
        double totalRecaudado = 0.0;

        for (int i = 0; i < totalVentas; i++) {
            // Sumar el valor de venta
            double valorVenta = 0.0;
            try {
                valorVenta = Double.parseDouble(tablaVentas.getValueAt(i, 3).toString());
            } catch (NumberFormatException e) {
                valorVenta = 0.0;
            }
            totalRecaudado += valorVenta;

            // Sumar la cantidad de productos en el detalle
            String detalle = tablaVentas.getValueAt(i, 2).toString(); // ej: "2x Paracetamol, 1x Ibuprofeno"
            String[] productos = detalle.split(","); // separar cada producto
            for (String p : productos) {
                p = p.trim();
                if (p.contains("x")) {
                    try {
                        int cantidad = Integer.parseInt(p.split("x")[0].trim());
                        totalProductos += cantidad;
                    } catch (NumberFormatException ex) {
                        // ignorar si no se puede parsear
                    }
                }
            }
        }

        // Actualizar etiquetas
        lblTotalVentas.setText(String.valueOf(totalVentas));
        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblTotalRecaudado.setText(String.format("%.2f", totalRecaudado));
    }

}
