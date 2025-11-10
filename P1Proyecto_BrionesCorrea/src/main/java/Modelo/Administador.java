package Modelo;

public class Administador extends Usuario {

    public Administador(String usuario, String contrasena) {
        super(usuario, contrasena);
    }
    
    @Override
    public String getRol() {
        return "Administrador";
    }
    
}
