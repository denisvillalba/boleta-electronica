package pe.vr.boleta.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import pe.vr.boleta.model.Boleta;

public class BoletaXmlServiceTest {

        @Test
    void generaXmlDePrueba() throws Exception {
        // 1. Boleta de ejemplo (por ahora solo el número)
        Boleta boleta = new Boleta();
        boleta.setNumero("B001-00000001");
        boleta.setFecha("15/11/2026 21:10");

        // 2. Pedimos al servicio que arme el XML
        BoletaXmlService servicio = new BoletaXmlService(
                "Mi Negocio de Prueba",        // negocioNombre
                "20123456789",                 // negocioRuc (11 dígitos)
                "Av. Prueba 123, Huancayo");   // negocioDireccion
        String xml = servicio.generar(boleta);

        // 3. Guardamos el XML en target/boleta-prueba.xml para verlo
        Files.writeString(Path.of("target/boleta-prueba.xml"), xml);

        // 4. Comprobamos que tenga la raíz <Invoice>
        assertTrue(xml.contains("Invoice"));
    }

    @Test
    void generaXmlConDni() throws Exception {
        // 1. Boleta de ejemplo CON datos del cliente
        Boleta boleta = new Boleta();
        boleta.setNumero("B001-00000002");
        boleta.setFecha("22/11/2026 10:30");
        boleta.setClienteDni("12345678");                 // DNI del cliente (8 dígitos)
        boleta.setClienteNombre("Juan Perez");            // nombre del cliente

        // 2. Pedimos al servicio que arme el XML
        BoletaXmlService servicio = new BoletaXmlService(
                "Mi Negocio de Prueba", "20123456789", "Av. Prueba 123, Huancayo");
        String xml = servicio.generar(boleta);

        // 3. Lo guardamos aparte para no pisar el XML sin DNI
        Files.writeString(Path.of("target/boleta-prueba-dni.xml"), xml);

        // 4. Comprobamos el bloque del cliente
        assertTrue(xml.contains("<cbc:ID schemeID=\"1\">12345678</cbc:ID>"));            // 1 = DNI + el número
        assertTrue(xml.contains("<cbc:RegistrationName>Juan Perez</cbc:RegistrationName>")); // nombre del cliente
    }

    @Test
    void generaXmlConTotales() throws Exception {
        // 1. Boleta de ejemplo con un total de S/ 118.00 (IGV incluido)
        Boleta boleta = new Boleta();
        boleta.setNumero("B001-00000003");
        boleta.setFecha("24/11/2026 15:30");
        boleta.setTotal(118.0);                                     // total cobrado al cliente

        // 2. Pedimos al servicio que arme el XML
        BoletaXmlService servicio = new BoletaXmlService(
                "Mi Negocio de Prueba", "20123456789", "Av. Prueba 123, Huancayo");
        String xml = servicio.generar(boleta);

        // 3. Lo guardamos aparte para revisarlo
        Files.writeString(Path.of("target/boleta-prueba-totales.xml"), xml);

        // 4. Comprobamos que los montos cuadren: 100.00 + 18.00 = 118.00
        assertTrue(xml.contains("<cbc:TaxableAmount currencyID=\"PEN\">100.00</cbc:TaxableAmount>")); // base sin IGV
        assertTrue(xml.contains("<cbc:TaxAmount currencyID=\"PEN\">18.00</cbc:TaxAmount>"));          // IGV
        assertTrue(xml.contains("<cbc:PayableAmount currencyID=\"PEN\">118.00</cbc:PayableAmount>")); // total a pagar
    }
    
}
