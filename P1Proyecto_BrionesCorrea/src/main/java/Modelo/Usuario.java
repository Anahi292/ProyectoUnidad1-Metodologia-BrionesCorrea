
package Modelo;

public abstract class Usuario {
    private String usuario;
    private String contrasena;

    public Usuario(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;

    }
    public String getUsuario() {
        return usuario; 
    }
    public String getContrasena() {
        return contrasena; 
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena; 
    }
    // Método común que se puede sobrescribir
    public abstract String getRol();

}
