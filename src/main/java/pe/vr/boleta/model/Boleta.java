package pe.vr.boleta.model;

import java.util.ArrayList;
import java.util.List;

public class Boleta {

    private String numero;
    private String fecha;
    private String clienteDni;
    private String clienteNombre;
    private String formaPago;
    private double total;
    private List<DetalleBoleta> detalles = new ArrayList<>();

    

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public List<DetalleBoleta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleBoleta> detalles) {
        this.detalles = detalles;
    }

    public void agregarDetalle(DetalleBoleta detalle) {
        detalle.calcularSubtotal();
        detalles.add(detalle);
        calcularTotal();
    }

    public void calcularTotal() {
        double suma = 0;
        for (DetalleBoleta d : detalles) {
            suma += d.getSubtotal();
        }
        this.total = suma;
    }

}