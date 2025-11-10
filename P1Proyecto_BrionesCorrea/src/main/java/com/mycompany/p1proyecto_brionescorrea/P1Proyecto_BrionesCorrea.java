/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.p1proyecto_brionescorrea;

import Vista.Login;
import Controlador.ControladorLogin;

public class P1Proyecto_BrionesCorrea {

    public static void main(String[] args) {
        Login login = new Login();
        ControladorLogin controlador = new ControladorLogin(login);
        login.setVisible(true);
        login.setLocationRelativeTo(null);
    }
}
