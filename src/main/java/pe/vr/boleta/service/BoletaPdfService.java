package pe.vr.boleta.service;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import pe.vr.boleta.model.Boleta;
import pe.vr.boleta.model.DetalleBoleta;

import org.springframework.beans.factory.annotation.Value;

import com.lowagie.text.Element;
import com.lowagie.text.Font;

import com.lowagie.text.Chunk;
import com.lowagie.text.Phrase;

import java.awt.Color;

@Service
public class BoletaPdfService {

    private final String negocioNombre;
    private final String negocioRuc;
    private final String negocioDireccion;

    public BoletaPdfService(@Value("${negocio.nombre}") String negocioNombre,
                            @Value("${negocio.ruc}") String negocioRuc,
                            @Value("${negocio.direccion}") String negocioDireccion) {
        this.negocioNombre = negocioNombre;
        this.negocioRuc = negocioRuc;
        this.negocioDireccion = negocioDireccion;
    }

    public byte[] generar(Boleta boleta) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, salida);

        documento.open();
        
        // Tipos de letra
        Font fTitulo = new Font(Font.HELVETICA, 14, Font.BOLD);
        Font fNormal = new Font(Font.HELVETICA, 11, Font.NORMAL);
        Font fNegrita = new Font(Font.HELVETICA, 11, Font.BOLD);

        // Encabezado: tabla de 2 columnas (negocio | recuadro)
        PdfPTable encabezado = new PdfPTable(2);
        encabezado.setWidthPercentage(100);
        encabezado.setWidths(new float[] { 60, 40 });

        // Columna izquierda: datos del negocio, sin borde
        PdfPCell celdaNegocio = new PdfPCell();
        celdaNegocio.setBorder(Rectangle.NO_BORDER);
        celdaNegocio.addElement(new Paragraph(negocioNombre, fTitulo));
        celdaNegocio.addElement(new Paragraph(negocioDireccion, fNormal));
        encabezado.addCell(celdaNegocio);

        // Columna derecha: recuadro con RUC, tipo y número
        PdfPCell celdaRecuadro = new PdfPCell();
        celdaRecuadro.setBorderWidth(1.5f);
        celdaRecuadro.setPadding(8);
        celdaRecuadro.addElement(centrado("RUC: " + negocioRuc, fNegrita));
        celdaRecuadro.addElement(centrado("BOLETA DE VENTA ELECTRÓNICA", fNegrita));
        celdaRecuadro.addElement(centrado(boleta.getNumero(), fNegrita));
        encabezado.addCell(celdaRecuadro);

        documento.add(encabezado);

        // Datos de la venta y del cliente: tabla de 2 columnas sin bordes
        String cliente = boleta.getClienteNombre() != null ? boleta.getClienteNombre() : "Clientes varios";
        String dni = boleta.getClienteDni() != null ? boleta.getClienteDni() : "-";

        PdfPTable datos = new PdfPTable(2);
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(15);
        datos.setSpacingAfter(10);
        datos.addCell(celdaDato("Fecha de emisión: ", boleta.getFecha(), fNegrita, fNormal));
        datos.addCell(celdaDato("Forma de pago: ", boleta.getFormaPago(), fNegrita, fNormal));
        datos.addCell(celdaDato("Señor(es): ", cliente, fNegrita, fNormal));
        datos.addCell(celdaDato("DNI: ", dni, fNegrita, fNormal));

        documento.add(datos);

                // Detalle de productos: tabla de 6 columnas
        PdfPTable detalle = new PdfPTable(6);
        detalle.setWidthPercentage(100);
        detalle.setWidths(new float[] { 11, 39, 14, 10, 13, 13 });
        detalle.setSpacingBefore(10);

        // Fila de títulos (fondo gris)
        String[] titulos = { "Código", "Descripción", "U.M.", "Cant.", "P. Unit.", "Subtotal" };
        for (String titulo : titulos) {
            PdfPCell celda = new PdfPCell(new Phrase(titulo, fNegrita));
            celda.setBackgroundColor(new Color(230, 230, 230));
            celda.setHorizontalAlignment(Element.ALIGN_CENTER);
            celda.setPadding(5);
            detalle.addCell(celda);
        }
        detalle.setHeaderRows(1);

        // Una fila por cada producto vendido
        for (DetalleBoleta d : boleta.getDetalles()) {
            detalle.addCell(celdaTabla(d.getCodigo(), fNormal, Element.ALIGN_CENTER));
            detalle.addCell(celdaTabla(d.getNombre(), fNormal, Element.ALIGN_LEFT));
            detalle.addCell(celdaTabla(d.getUnidadMedida(), fNormal, Element.ALIGN_CENTER));
            detalle.addCell(celdaTabla(String.valueOf(d.getCantidad()), fNormal, Element.ALIGN_CENTER));
            detalle.addCell(celdaTabla(dinero(d.getPrecioUnitario()), fNormal, Element.ALIGN_RIGHT));
            detalle.addCell(celdaTabla(dinero(d.getSubtotal()), fNormal, Element.ALIGN_RIGHT));
        }

        documento.add(detalle);

                // Totales: el precio ya incluye IGV, así que se separa
        double total = boleta.getTotal();
        double gravada = Math.round(total / 1.18 * 100) / 100.0;
        double igv = Math.round((total - gravada) * 100) / 100.0;

        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(40);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setSpacingBefore(10);

        totales.addCell(celdaTabla("Op. gravada", fNormal, Element.ALIGN_LEFT));
        totales.addCell(celdaTabla("S/ " + dinero(gravada), fNormal, Element.ALIGN_RIGHT));
        totales.addCell(celdaTabla("IGV (18%)", fNormal, Element.ALIGN_LEFT));
        totales.addCell(celdaTabla("S/ " + dinero(igv), fNormal, Element.ALIGN_RIGHT));

        PdfPCell lblTotal = celdaTabla("IMPORTE TOTAL", fNegrita, Element.ALIGN_LEFT);
        PdfPCell montoTotal = celdaTabla("S/ " + dinero(total), fNegrita, Element.ALIGN_RIGHT);
        lblTotal.setBackgroundColor(new Color(230, 230, 230));
        montoTotal.setBackgroundColor(new Color(230, 230, 230));
        totales.addCell(lblTotal);
        totales.addCell(montoTotal);

        documento.add(totales);

        documento.close();

        return salida.toByteArray();
    }

    private Paragraph centrado(String texto, Font fuente) {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(Element.ALIGN_CENTER);
        return p;
    }

    private PdfPCell celdaDato(String etiqueta, String valor, Font fEtiqueta, Font fValor) {
        Phrase frase = new Phrase();
        frase.add(new Chunk(etiqueta, fEtiqueta));
        frase.add(new Chunk(valor, fValor));

        PdfPCell celda = new PdfPCell(frase);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPaddingBottom(4);
        return celda;
    }

    private PdfPCell celdaTabla(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setHorizontalAlignment(alineacion);
        celda.setPadding(5);
        return celda;
    }

    private String dinero(double monto) {
        return String.format(Locale.US, "%.2f", monto);
    }

}