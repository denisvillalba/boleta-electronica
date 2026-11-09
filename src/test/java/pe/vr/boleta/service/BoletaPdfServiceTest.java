package pe.vr.boleta.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import pe.vr.boleta.model.Boleta;

import pe.vr.boleta.model.DetalleBoleta;

class BoletaPdfServiceTest {

    @Test
    void generaPdfDePrueba() throws Exception {
        BoletaPdfService pdfService = new BoletaPdfService(
                "MI NEGOCIO S.A.C.", "20000000000", "Av. Ejemplo 123, Lima");

        Boleta boleta = new Boleta();
        boleta.setNumero("B001-00000099");
        boleta.setFecha("09/11/2026 01:40");
        boleta.setClienteNombre("Cliente de prueba");
        boleta.setClienteDni("12345678");
        boleta.setFormaPago("Efectivo");

        DetalleBoleta arroz = new DetalleBoleta();
        arroz.setCodigo("P001");
        arroz.setNombre("Arroz");
        arroz.setUnidadMedida("Kilogramo");
        arroz.setCantidad(2);
        arroz.setPrecioUnitario(4.80);
        arroz.calcularSubtotal();
        boleta.agregarDetalle(arroz);

        DetalleBoleta aceite = new DetalleBoleta();
        aceite.setCodigo("P003");
        aceite.setNombre("Aceite vegetal 1 L");
        aceite.setUnidadMedida("Unidad");
        aceite.setCantidad(1);
        aceite.setPrecioUnitario(10.00);
        aceite.calcularSubtotal();
        boleta.agregarDetalle(aceite);

        boleta.calcularTotal();

        byte[] pdf = pdfService.generar(boleta);

        assertTrue(pdf.length > 0);
        Files.write(Path.of("target/boleta-prueba.pdf"), pdf);
    }
}