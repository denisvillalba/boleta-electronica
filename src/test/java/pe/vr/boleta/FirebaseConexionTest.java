package pe.vr.boleta;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.cloud.firestore.Firestore;

import pe.vr.boleta.config.FirebaseConfig;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;

class FirebaseConexionTest {

    @Test
    void guardaYLeeUnDocumento() throws Exception {

        assumeTrue(new File("D:/claves/boleta-electronica.json").exists(), "No hay clave de Firebase; se omite la prueba");
        FirebaseConfig config = new FirebaseConfig();
        ReflectionTestUtils.setField(config, "rutaCredenciales", "D:/claves/boleta-electronica.json");
        Firestore db = config.firestore();

        db.collection("pruebas").document("conexion")
          .set(Map.of("mensaje", "Hola desde Java")).get();

        String leido = db.collection("pruebas").document("conexion")
          .get().get().getString("mensaje");

        assertEquals("Hola desde Java", leido);
    }
}