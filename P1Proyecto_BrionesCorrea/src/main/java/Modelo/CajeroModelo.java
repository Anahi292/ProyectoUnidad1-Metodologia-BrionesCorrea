
package Modelo;

public class CajeroModelo extends Usuario {
 public CajeroModelo(String usuario, String contrasena) {
        super(usuario, contrasena);
    }

    @Override
    public String getRol() {
        return "Cajero";
    }
}
