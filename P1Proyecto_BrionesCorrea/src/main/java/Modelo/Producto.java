package Modelo;

public class Producto {

    private String codigo;
    private String nombre;
    private int stock;
    private double precio;
    private String Caducidad;
    private Proveedor proveedor;

    public Producto(String codigo, String nombre, int stock, double precio, String Caducidad, Proveedor proveedor) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.stock = stock;
        this.precio = precio;
        this.Caducidad = Caducidad;
        this.proveedor = proveedor;
    }
    //constrcutor vacio
    public Producto() {
        // No hace nada, permite crear el objeto y luego usar setters
    }
    public String getCodigo() {
        return codigo;
    }
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public double getPrecio() {
        return precio;
    }
    public void setPrecio(double precio) {
        this.precio = precio;
    }
    public int getStock() {
        return stock;
    }
    public void setStock(int stock) {
        this.stock = stock;
    }
    public Proveedor getProveedor() {
        return proveedor;
    }
    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }
    public String getCaducidad() {
        return Caducidad;
    }
    public void setCaducidad(String Caducidad) {
        this.Caducidad = Caducidad;
    }
    //METODO PROPIO
    public void disminuirStock(int cantidad) {
        this.stock -= cantidad;
    }
}
