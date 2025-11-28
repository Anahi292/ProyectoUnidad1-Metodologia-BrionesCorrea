package Controlador;

import Modelo.*;
import Vista.*;
import java.awt.event.*;
import javax.swing.*;

public class ControladorLogin implements ActionListener {

    private Login vista;

    public ControladorLogin(Login vista) {
        this.vista = vista;

        // Escucha el botón de inicio de sesión
        this.vista.btnIniciar.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.btnIniciar) {
            iniciarSesion();
        }
    }

    private void iniciarSesion() {
        String usuario = vista.txtUsuario.getText();
        String contrasena = vista.txtContrasena.getText();

        //  AQUÍ LLAMAMOS A MONGODB
        Usuario user = MongoDB.buscarUsuario(usuario, contrasena);

        if (user != null) {

            JOptionPane.showMessageDialog(vista,
                    "Bienvenido, " + user.getUsuario() + " (" + user.getRol() + ")");

            // Redirigir según el rol
            switch (user.getRol().toLowerCase()) {

                case "administrador":
                    AdministradorInicio adminVista = new AdministradorInicio();
                    adminVista.setVisible(true);
                    vista.dispose();
                    break;

                case "cajero":
                    Cajero cajeroVista = new Cajero();
                    cajeroVista.setVisible(true);
                    vista.dispose();
                    break;

                default:
                    JOptionPane.showMessageDialog(vista, "Rol no reconocido");
                    break;
            }

        } else {
            JOptionPane.showMessageDialog(vista, "Usuario o contraseña incorrectos");
        }
    }
}
