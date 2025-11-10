package Controlador;

import Modelo.Administador;
import Modelo.CajeroModelo;
import Modelo.Usuario;
import Vista.Login;
import Vista.AdministradorInicio;
import Vista.Cajero;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;

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

        Usuario user = autenticarUsuario(usuario, contrasena);

        if (user != null) {
            JOptionPane.showMessageDialog(vista,
                    "Bienvenido, " + user.getUsuario() + " (" + user.getRol() + ")");

            // Redirigir según el rol
            if (user instanceof Administador) {
                AdministradorInicio adminVista = new AdministradorInicio();
                adminVista.setVisible(true);
                vista.dispose();
            } else if (user instanceof CajeroModelo) {
               
                Cajero cajeroVista = new Cajero();
                cajeroVista.setVisible(true);
                vista.dispose();
            }

        } else {
            JOptionPane.showMessageDialog(vista, "Usuario o contraseña incorrectos");
        }
    }

    // Aquí puedes cambiar por una conexión a base de datos si luego la agregas
    private Usuario autenticarUsuario(String usuario, String contrasena) {
        // Ejemplo con datos fijos
        if (usuario.equals("admin") && contrasena.equals("1234")) {
            return new Administador(usuario, contrasena);
        } else if (usuario.equals("cajero") && contrasena.equals("abcd")) {
            return new CajeroModelo(usuario, contrasena);
        }
        return null; // No coincide
    }
}
