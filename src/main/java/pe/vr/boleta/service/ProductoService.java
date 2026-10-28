package pe.vr.boleta.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import pe.vr.boleta.model.Producto;

import com.google.cloud.firestore.Firestore;

@Service
public class ProductoService {

    private final Firestore db;

       public ProductoService(Firestore db) {
           this.db = db;
    }

    private final List<Producto> productos = new ArrayList<>();

    public List<Producto> listar() {
        try {
            return db.collection("productos").get().get().toObjects(Producto.class);
        } catch (Exception e) {
            throw new RuntimeException("No se pudieron leer los productos de Firestore", e);
        }
    }

    public String siguienteCodigo() {
        int mayor = 0;
        for (Producto p : listar()) {
            int numero = Integer.parseInt(p.getCodigo().substring(1));
            if (numero > mayor) {
                mayor = numero;
            }
        }
        return String.format("P%03d", mayor + 1);
    }

    public void guardar(Producto producto) {
        productos.add(producto);
        try {
            db.collection("productos").document(producto.getCodigo()).set(producto).get();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el producto en Firestore", e);
        }
    }

        public Producto buscarPorCodigo(String codigo) {
        for (Producto p : productos) {
            if (p.getCodigo().equals(codigo)) {
                return p;
            }
        }
        return null;
    }

    public void descontarStock(String codigo, int cantidad) {
        Producto p = buscarPorCodigo(codigo);
        if (p == null) {
            throw new IllegalArgumentException("No existe el producto: " + codigo);
        }
        if (p.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente de " + p.getNombre()
                    + ". Disponible: " + p.getStock());
        }
        p.setStock(p.getStock() - cantidad);

        try {
            db.collection("productos").document(codigo).set(p).get();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo actualizar el stock del producto en Firestore", e);
        }

    }
}