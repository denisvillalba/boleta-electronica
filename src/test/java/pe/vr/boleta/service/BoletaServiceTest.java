package pe.vr.boleta.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pe.vr.boleta.model.Boleta;
import pe.vr.boleta.model.DetalleBoleta;
import pe.vr.boleta.model.Producto;

class BoletaServiceTest {

    private ProductoService productoService;
    private BoletaService boletaService;

    @BeforeEach
    void preparar() {
        productoService = mock(ProductoService.class);
        boletaService = new BoletaService(productoService);
        when(productoService.buscarPorCodigo("P001")).thenReturn(producto("P001", "Vitamina", 10));
        when(productoService.buscarPorCodigo("P002")).thenReturn(producto("P002", "Alimento", 2));
    }

    @Test
    void emiteBoletaYDescuentaStock() {
        Boleta b = new Boleta();
        b.agregarDetalle(detalle("P001", 3, 15.00));

        Boleta emitida = boletaService.emitir(b);

        assertEquals("B001-00000001", emitida.getNumero());
        assertNotNull(emitida.getFecha());
        assertEquals(45.00, emitida.getTotal(), 0.001);
        verify(productoService).descontarStock("P001", 3);
        assertEquals(1, boletaService.listar().size());
    }

    @Test
    void correlativoAumenta() {
        Boleta b1 = new Boleta();
        b1.agregarDetalle(detalle("P001", 1, 15.00));
        Boleta b2 = new Boleta();
        b2.agregarDetalle(detalle("P001", 1, 15.00));

        boletaService.emitir(b1);
        Boleta segunda = boletaService.emitir(b2);

        assertEquals("B001-00000002", segunda.getNumero());
    }

    @Test
    void noEmiteBoletaVacia() {
        assertThrows(IllegalArgumentException.class,
                () -> boletaService.emitir(new Boleta()));
    }

    @Test
    void siUnProductoNoAlcanzaNoDescuentaNinguno() {
        Boleta b = new Boleta();
        b.agregarDetalle(detalle("P001", 3, 15.00));
        b.agregarDetalle(detalle("P002", 5, 80.00));

        assertThrows(IllegalArgumentException.class, () -> boletaService.emitir(b));

                verify(productoService, never()).descontarStock(anyString(), anyInt());
        assertEquals(0, boletaService.listar().size());
    }

    private Producto producto(String codigo, String nombre, int stock) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setStock(stock);
        return p;
    }

    private DetalleBoleta detalle(String codigo, int cantidad, double precio) {
        DetalleBoleta d = new DetalleBoleta();
        d.setCodigo(codigo);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precio);
        return d;
    }
}