package pe.vr.boleta.config;

import java.io.FileInputStream;                               // ← nuevo
import java.io.IOException;                                   // ← nuevo

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;           // ← nuevo
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;              // ← nuevo
import com.google.cloud.firestore.Firestore;                  // ← nuevo
import com.google.firebase.FirebaseApp;                       // ← nuevo
import com.google.firebase.FirebaseOptions;                   // ← nuevo
import com.google.firebase.cloud.FirestoreClient;             // ← nuevo

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credenciales}")
    private String rutaCredenciales;

    @Bean                                                                    // ← nuevo
    public Firestore firestore() throws IOException {                        // ← nuevo
        try (FileInputStream archivo = new FileInputStream(rutaCredenciales)) {
            GoogleCredentials credenciales = GoogleCredentials.fromStream(archivo);
            FirebaseOptions opciones = FirebaseOptions.builder()
                    .setCredentials(credenciales)
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(opciones);
            }
        }
        return FirestoreClient.getFirestore();
    }
}