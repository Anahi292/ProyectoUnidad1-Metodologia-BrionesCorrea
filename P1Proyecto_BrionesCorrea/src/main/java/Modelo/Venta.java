
package Modelo;

public class Venta {
    private String numeroVenta;
    private String hora;
    private String detalleProductos;
    private double valorVenta;

    public Venta(String numeroVenta, String hora, String detalleProductos, double valorVenta) {
        this.numeroVenta = numeroVenta;
        this.hora = hora;
        this.detalleProductos = detalleProductos;
        this.valorVenta = valorVenta;
    }
    public Venta(){
        
    }
    // Getters y Setters
    public String getNumeroVenta() {
        return numeroVenta;
    }
    public void setNumeroVenta(String numeroVenta) {
        this.numeroVenta = numeroVenta;
    }
    public String getHora() {
        return hora;
    }
    public void setHora(String hora) {
        this.hora = hora;
    }
    public String getDetalleProductos() {
        return detalleProductos;
    }
    public void setDetalleProductos(String detalleProductos) {
        this.detalleProductos = detalleProductos;
    }
    public double getValorVenta() {
        return valorVenta;
    }
    public void setValorVenta(double valorVenta) {
        this.valorVenta = valorVenta;
    }
    
}
