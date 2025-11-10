package Controlador;

import Modelo.Proveedor;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author PERSONAL
 */
public class ControladorProveedores {

    private List<Proveedor> listaProveedores = new ArrayList<>();

    public void cargarExcelEnTablaProveedores(String rutaArchivo, JTable tablaProveedores) {
        try (FileInputStream file = new FileInputStream(new File(rutaArchivo)); Workbook workbook = new XSSFWorkbook(file)) {

            Sheet hoja = workbook.getSheetAt(0);
            DefaultTableModel modelo = (DefaultTableModel) tablaProveedores.getModel();
            modelo.setRowCount(0); // Limpia la tabla
            listaProveedores.clear(); // Limpia la lista actual

            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila != null) {
                    // Lee las celdas como texto
                    String id = fila.getCell(0).toString().trim();
                    String nombre = fila.getCell(1).toString().trim();
                    String telefono = fila.getCell(2).toString().trim();
                    String direccion = fila.getCell(3).toString().trim();

                    // Agrega a la tabla
                    modelo.addRow(new Object[]{id, nombre, telefono, direccion});

                    // Crea objeto proveedor
                    Proveedor p = new Proveedor();
                    p.setIdProveedor(id);
                    p.setNombre(nombre);
                    p.setTelefono(telefono);
                    p.setDireccion(direccion);

                    // Agrega a la lista
                    listaProveedores.add(p);
                }
            }

            tablaProveedores.setModel(modelo);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Error al cargar el archivo Excel de proveedores.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void buscarProveedor(JTable tablaProveedores, String criterio) {
        DefaultTableModel modelo = (DefaultTableModel) tablaProveedores.getModel();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        tablaProveedores.setRowSorter(sorter);

        if (criterio == null || criterio.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }

        criterio = criterio.trim();
        RowFilter<DefaultTableModel, Object> filtro = RowFilter.regexFilter("(?i)" + criterio, 0, 1);
        sorter.setRowFilter(filtro);

        if (sorter.getViewRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Proveedor no encontrado.", "Búsqueda", JOptionPane.WARNING_MESSAGE);
            sorter.setRowFilter(null);
        }
    }

    public void eliminarProveedor(JTable tablaProveedores, String rutaExcel) {
        int fila = tablaProveedores.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un proveedor.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(null, "¿Eliminar proveedor seleccionado?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            DefaultTableModel modelo = (DefaultTableModel) tablaProveedores.getModel();
            String idEliminar = modelo.getValueAt(fila, 0).toString();

            // Eliminar de la lista interna
            boolean eliminado = listaProveedores.removeIf(p -> p.getIdProveedor().equals(idEliminar));

            if (eliminado) {
                // Actualizar la tabla
                modelo.removeRow(fila);

                // Guardar la lista actualizada en el Excel
                guardarProveedoresEnExcel(listaProveedores, rutaExcel);

                JOptionPane.showMessageDialog(null, "Proveedor eliminado correctamente.");
            } else {
                JOptionPane.showMessageDialog(null, "No se encontró el proveedor en la lista.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al eliminar proveedor: " + e.getMessage());
        }
    }

    public void eliminarTodosProveedores(JTable tablaProveedores, String rutaExcel) {
        int opcion = JOptionPane.showConfirmDialog(null,
                "¿Está seguro de eliminar TODOS los proveedores?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Limpiar lista y tabla
            listaProveedores.clear();
            DefaultTableModel modelo = (DefaultTableModel) tablaProveedores.getModel();
            modelo.setRowCount(0);

            // Guardar cambios en el Excel
            guardarProveedoresEnExcel(listaProveedores, rutaExcel);
            JOptionPane.showMessageDialog(null, "Todos los proveedores fueron eliminados correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al eliminar los proveedores.");
        }
    }

    public void guardarProveedoresEnExcel(List<Proveedor> lista, String rutaArchivo) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet hoja = workbook.createSheet("Proveedores");

        Row header = hoja.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Nombre");
        header.createCell(2).setCellValue("Teléfono");
        header.createCell(3).setCellValue("Dirección");

        int fila = 1;
        for (Proveedor p : lista) {
            Row row = hoja.createRow(fila++);
            row.createCell(0).setCellValue(p.getIdProveedor());
            row.createCell(1).setCellValue(p.getNombre());
            row.createCell(2).setCellValue(p.getTelefono());
            row.createCell(3).setCellValue(p.getDireccion());
        }

        try (FileOutputStream out = new FileOutputStream(rutaArchivo)) {
            workbook.write(out);
        }
        workbook.close();
    }

    public void modificarProveedor(JTable tabla, String rutaExcel) {
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(null, "Seleccione un proveedor para modificar.");
            return;
        }

        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        String id = modelo.getValueAt(fila, 0).toString();

        try {
            String nombre = JOptionPane.showInputDialog("Nuevo nombre:", modelo.getValueAt(fila, 1));
            String telefono = JOptionPane.showInputDialog("Nuevo teléfono:", modelo.getValueAt(fila, 2));
            String direccion = JOptionPane.showInputDialog("Nueva dirección:", modelo.getValueAt(fila, 3));

            if (nombre == null || telefono == null || direccion == null) {
                return; // Cancelado
            }
            // Actualizar en la tabla
            modelo.setValueAt(nombre, fila, 1);
            modelo.setValueAt(telefono, fila, 2);
            modelo.setValueAt(direccion, fila, 3);

            // Actualizar en la lista
            for (Proveedor p : listaProveedores) {
                if (p.getIdProveedor().equals(id)) {
                    p.setNombre(nombre);
                    p.setTelefono(telefono);
                    p.setDireccion(direccion);
                    break;
                }
            }

            // Guardar cambios en Excel
            guardarProveedoresEnExcel(listaProveedores, rutaExcel);
            JOptionPane.showMessageDialog(null, "Proveedor modificado correctamente.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al modificar proveedor.");
        }
    }

    public void agregarProveedor(JTextField txtId, JTextField txtNombre, JTextField txtTelefono,
            JTextField txtDireccion, JTable tablaProveedores, String rutaExcel) {
        try {
            String id = txtId.getText().trim();
            String nombre = txtNombre.getText().trim();
            String telefono = txtTelefono.getText().trim();
            String direccion = txtDireccion.getText().trim();

            // 🔹 Validar que ningún campo esté vacío
            if (id.isEmpty() || nombre.isEmpty() || telefono.isEmpty() || direccion.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Todos los campos son obligatorios.");
                return;
            }

            // 🔹 Validar nombre (solo letras y espacios)
            if (!nombre.matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+")) {
                JOptionPane.showMessageDialog(null, "El nombre solo debe contener letras y espacios.");
                return;
            }

            // 🔹 Validar teléfono (solo números, longitud mínima y máxima opcional)
            if (!telefono.matches("\\d{7,10}")) { // Por ejemplo: entre 7 y 10 dígitos
                JOptionPane.showMessageDialog(null, "El teléfono debe contener solo números (7 a 10 dígitos).");
                return;
            }

            // 🔹 Validar dirección (mínimo 5 caracteres)
            if (direccion.length() < 5) {
                JOptionPane.showMessageDialog(null, "La dirección es muy corta. Ingrese una dirección válida.");
                return;
            }

            // 🔹 Verificar si el ID ya existe en la lista
            for (Proveedor p : listaProveedores) {
                if (p.getIdProveedor().equalsIgnoreCase(id)) {
                    JOptionPane.showMessageDialog(null, "El ID del proveedor ya existe.");
                    return;
                }
            }

            // 🔹 Crear nuevo proveedor
            Proveedor nuevo = new Proveedor();
            nuevo.setIdProveedor(id);
            nuevo.setNombre(nombre);
            nuevo.setTelefono(telefono);
            nuevo.setDireccion(direccion);

            // 🔹 Agregar a la lista
            listaProveedores.add(nuevo);

            // 🔹 Agregar a la tabla
            DefaultTableModel modelo = (DefaultTableModel) tablaProveedores.getModel();
            modelo.addRow(new Object[]{id, nombre, telefono, direccion});

            // 🔹 Guardar cambios en Excel
            guardarProveedoresEnExcel(listaProveedores, rutaExcel);

            // 🔹 Limpiar campos
            txtId.setText("");
            txtNombre.setText("");
            txtTelefono.setText("");
            txtDireccion.setText("");

            JOptionPane.showMessageDialog(null, "Proveedor agregado correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al agregar proveedor.");
        }
    }

    public void limpiarBusqueda(JTable tablaProveedores) {
        DefaultTableModel modelo = (DefaultTableModel) tablaProveedores.getModel();
        TableRowSorter<DefaultTableModel> sorter = (TableRowSorter<DefaultTableModel>) tablaProveedores.getRowSorter();

        if (sorter != null) {
            sorter.setRowFilter(null); // Elimina cualquier filtro aplicado
        }

        // Limpiar tabla
        modelo.setRowCount(0);

        // Volver a cargar toda la lista interna
        for (Proveedor p : listaProveedores) {
            modelo.addRow(new Object[]{p.getIdProveedor(), p.getNombre(), p.getTelefono(), p.getDireccion()});
        }

        tablaProveedores.setModel(modelo);
    }

}
