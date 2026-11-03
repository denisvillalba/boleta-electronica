package pe.vr.boleta.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import pe.vr.boleta.model.Boleta;
import pe.vr.boleta.model.DetalleBoleta;
import pe.vr.boleta.model.Producto;

import com.google.cloud.firestore.Firestore;

import java.util.Map;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;

@Service
public class BoletaService {

    private static final String SERIE = "B001";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final List<Boleta> boletas = new ArrayList<>();
    private final ProductoService productoService;
    private final Firestore db;
    
    public BoletaService(ProductoService productoService, Firestore db) {
        this.productoService = productoService;
        this.db = db;
    }

    public Boleta emitir(Boleta boleta) {
        if (boleta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La boleta no tiene productos.");
        }

        for (DetalleBoleta d : boleta.getDetalles()) {
            Producto p = productoService.buscarPorCodigo(d.getCodigo());
            if (p == null) {
                throw new IllegalArgumentException("No existe el producto: " + d.getCodigo());
            }
            if (p.getStock() < d.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre()
                        + ". Disponible: " + p.getStock());
            }
        }

        for (DetalleBoleta d : boleta.getDetalles()) {
            productoService.descontarStock(d.getCodigo(), d.getCantidad());
        }

        int numero = siguienteCorrelativo();
        boleta.setNumero(SERIE + "-" + String.format("%08d", numero));
        boleta.setFecha(LocalDateTime.now().format(FORMATO_FECHA));
        boleta.calcularTotal();

       

        try {
            db.collection("boletas").document(boleta.getNumero()).set(boleta).get();
            
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar la boleta en Firestore", e);
        }

        boletas.add(boleta);
        return boleta;
    }

    private int siguienteCorrelativo() {
        DocumentReference ref = db.collection("contadores").document("boletas");
        try {
            return db.runTransaction(tx -> {
                DocumentSnapshot doc = tx.get(ref).get();
                Long ultimo = doc.exists() ? doc.getLong("ultimo") : null;
                long nuevo = (ultimo == null ? 0 : ultimo) + 1;
                tx.set(ref, Map.of("ultimo", nuevo));
                return (int) nuevo;
            }).get();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo obtener el número de boleta", e);
        }
    }

    public List<Boleta> listar() {
        return boletas;
    }
}