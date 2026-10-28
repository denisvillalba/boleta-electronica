package pe.vr.boleta.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import pe.vr.boleta.model.Boleta;
import pe.vr.boleta.model.DetalleBoleta;
import pe.vr.boleta.model.Producto;

@Service
public class BoletaService {

    private static final String SERIE = "B001";
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final List<Boleta> boletas = new ArrayList<>();
    private final ProductoService productoService;
    private int correlativo = 0;

    public BoletaService(ProductoService productoService) {
        this.productoService = productoService;
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

        correlativo++;
        boleta.setNumero(SERIE + "-" + String.format("%08d", correlativo));
        boleta.setFecha(LocalDateTime.now().format(FORMATO_FECHA));
        boleta.calcularTotal();
        boletas.add(boleta);
        return boleta;
    }

    public List<Boleta> listar() {
        return boletas;
    }
}