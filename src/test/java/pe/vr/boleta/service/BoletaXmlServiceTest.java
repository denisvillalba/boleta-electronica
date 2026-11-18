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
    
}
