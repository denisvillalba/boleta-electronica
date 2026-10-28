package pe.vr.boleta.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BoletaTest {

    @Test
    void calculaSubtotalesYTotal() {
        DetalleBoleta d1 = new DetalleBoleta();
        d1.setNombre("Vitamina");
        d1.setCantidad(2);
        d1.setPrecioUnitario(15.00);

        DetalleBoleta d2 = new DetalleBoleta();
        d2.setNombre("Alimento balanceado");
        d2.setCantidad(1);
        d2.setPrecioUnitario(80.00);

        Boleta boleta = new Boleta();
        boleta.agregarDetalle(d1);
        boleta.agregarDetalle(d2);

        assertEquals(30.00, d1.getSubtotal(), 0.001);
        assertEquals(80.00, d2.getSubtotal(), 0.001);
        assertEquals(110.00, boleta.getTotal(), 0.001);
        assertEquals(2, boleta.getDetalles().size());
    }
}