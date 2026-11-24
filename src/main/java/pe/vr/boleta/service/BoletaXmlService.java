package pe.vr.boleta.service;

import org.springframework.stereotype.Service;

import pe.vr.boleta.model.Boleta;

import org.springframework.beans.factory.annotation.Value;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;       // BigDecimal (número decimal exacto): para montos en soles sin errores de redondeo
import java.math.RoundingMode;     // RoundingMode (regla de redondeo): HALF_UP = redondeo comercial (0.005 → 0.01)

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Service
public class BoletaXmlService {

    // Namespaces oficiales del estándar UBL 2.1 (deben ser exactos)
    private static final String NS_INVOICE =
            "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String NS_CAC =
            "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC =
            "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    // Formatos de fecha: el de la boleta (entrada) y los de la SUNAT (salida)
    private static final DateTimeFormatter FORMATO_BOLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Datos del negocio (emisor), leídos de application.properties
    private final String negocioNombre;
    private final String negocioRuc;
    private final String negocioDireccion;

    public BoletaXmlService(@Value("${negocio.nombre}") String negocioNombre,
                            @Value("${negocio.ruc}") String negocioRuc,
                            @Value("${negocio.direccion}") String negocioDireccion) {
        this.negocioNombre = negocioNombre;          // ej. "Mi Negocio de Prueba"
        this.negocioRuc = negocioRuc;                // ej. "20123456789"
        this.negocioDireccion = negocioDireccion;    // ej. "Av. ..., Huancayo"
    }

    private static final DateTimeFormatter FECHA_SUNAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter HORA_SUNAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // Recibe una boleta y devuelve su XML (UBL 2.1) como texto
    public String generar(Boleta boleta) {
        try {
            // 1. Documento XML vacío
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .newDocument();

            // 2. Etiqueta raíz <Invoice> con sus prefijos cac y cbc
            Element invoice = doc.createElementNS(NS_INVOICE, "Invoice");
            invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cac", NS_CAC);
            invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cbc", NS_CBC);
            doc.appendChild(invoice);

            
            // 3. Cabecera: versión UBL, versión SUNAT y número de la boleta
            cbc(doc, invoice, "UBLVersionID", "2.1");
            cbc(doc, invoice, "CustomizationID", "2.0");
            cbc(doc, invoice, "ID", boleta.getNumero());
            
            // 4. Fecha y hora de emisión (formato SUNAT)
            LocalDateTime fechaHora = LocalDateTime.parse(boleta.getFecha(), FORMATO_BOLETA);
            cbc(doc, invoice, "IssueDate", fechaHora.format(FECHA_SUNAT));
            cbc(doc, invoice, "IssueTime", fechaHora.format(HORA_SUNAT));

            
            // 5. Tipo de comprobante (03 = boleta) y tipo de operación (0101 = venta interna)
            Element tipo = cbc(doc, invoice, "InvoiceTypeCode", "03");
            tipo.setAttribute("listID", "0101");

            // 6. Moneda (PEN = soles)
            cbc(doc, invoice, "DocumentCurrencyCode", "PEN");


            // 7. Emisor (el negocio): RUC, razón social y dirección
            Element proveedor = cac(doc, invoice, "AccountingSupplierParty");
            Element party = cac(doc, proveedor, "Party");

            Element identificacion = cac(doc, party, "PartyIdentification");
            Element ruc = cbc(doc, identificacion, "ID", negocioRuc);
            ruc.setAttribute("schemeID", "6");                      // 6 = tipo de documento RUC

            Element legal = cac(doc, party, "PartyLegalEntity");
            cbc(doc, legal, "RegistrationName", negocioNombre);     // razón social
            Element domicilio = cac(doc, legal, "RegistrationAddress");
            cbc(doc, domicilio, "AddressTypeCode", "0000");         // 0000 = domicilio fiscal
            Element lineaDir = cac(doc, domicilio, "AddressLine");
            cbc(doc, lineaDir, "Line", negocioDireccion);           // direcció


            // 8. Cliente (AccountingCustomerParty): a quién se vendió
            String dni = boleta.getClienteDni();                              // DNI escrito en la pantalla (puede venir null = vacío)
            boolean hayDni = dni != null && !dni.isBlank();                   // true (verdadero) si sí se llenó el DNI
            String nombre = boleta.getClienteNombre();                        // nombre del cliente (puede venir null)

            Element cliente = cac(doc, invoice, "AccountingCustomerParty");   // caja del cliente, dentro de la raíz <Invoice>
            Element partyCli = cac(doc, cliente, "Party");                    // la persona que compra
            Element idCli = cac(doc, partyCli, "PartyIdentification");        // su documento de identidad
            Element numDoc = cbc(doc, idCli, "ID", hayDni ? dni : "-");       // número de DNI, o "-" si no hay
            numDoc.setAttribute("schemeID", hayDni ? "1" : "-");              // tipo de documento: 1 = DNI, "-" = clientes varios
            Element legalCli = cac(doc, partyCli, "PartyLegalEntity");        // caja de datos legales
            cbc(doc, legalCli, "RegistrationName",
            (nombre != null && !nombre.isBlank()) ? nombre : "VARIOS");       // nombre del cliente, o VARIOS si está vacío

            // 9. Montos (mismo cálculo que el PDF): total con IGV → gravada (sin IGV) e IGV
            BigDecimal total = new BigDecimal(String.valueOf(boleta.getTotal()))   // importe cobrado, ej. 118.0
                    .setScale(2, RoundingMode.HALF_UP);                            // con 2 decimales → 118.00
            BigDecimal gravada = total.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP); // sin IGV → 100.00
            BigDecimal igv = total.subtract(gravada);                              // IGV por resta → 18.00 (siempre cuadra)

            // 10. Impuestos (cac:TaxTotal): cuánto IGV tiene la boleta
            Element taxTotal = cac(doc, invoice, "TaxTotal");                // caja: total de impuestos
            monto(doc, taxTotal, "TaxAmount", igv);                          // IGV total → 18.00
            Element subtotal = cac(doc, taxTotal, "TaxSubtotal");            // detalle del impuesto
            monto(doc, subtotal, "TaxableAmount", gravada);                  // base imponible (sin IGV) → 100.00
            monto(doc, subtotal, "TaxAmount", igv);                          // IGV de esa base → 18.00
            Element categoria = cac(doc, subtotal, "TaxCategory");           // categoría del impuesto
            Element esquema = cac(doc, categoria, "TaxScheme");              // qué impuesto es
            cbc(doc, esquema, "ID", "1000");                                 // 1000 = IGV (catálogo 05 SUNAT)
            cbc(doc, esquema, "Name", "IGV");                                // nombre del tributo
            cbc(doc, esquema, "TaxTypeCode", "VAT");                         // VAT = impuesto al valor agregado

            // 11. Totales legales (cac:LegalMonetaryTotal): cuánto se cobra
            Element totales = cac(doc, invoice, "LegalMonetaryTotal");     // caja: totales de la boleta
            monto(doc, totales, "LineExtensionAmount", gravada);           // valor de venta sin IGV → 100.00
            monto(doc, totales, "TaxInclusiveAmount", total);              // total con IGV → 118.00
            monto(doc, totales, "PayableAmount", total);                   // importe a pagar → 118.00

            return aTexto(doc);
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el XML: " + e.getMessage(), e);
        }
    }

    // Convierte el árbol XML (en memoria) en texto
    private String aTexto(Document doc) throws Exception {
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter salida = new StringWriter();
        t.transform(new DOMSource(doc), new StreamResult(salida));
        return salida.toString();
    }

    // Crea <cbc:nombre>valor</cbc:nombre> y lo mete dentro de "padre"
    private Element cbc(Document doc, Element padre, String nombre, String valor) {
        Element e = doc.createElementNS(NS_CBC, "cbc:" + nombre);
        e.setTextContent(valor);
        padre.appendChild(e);
        return e;
    }

    // Crea <cac:nombre> (una "caja" sin valor que agrupa otras etiquetas) dentro de "padre"
    private Element cac(Document doc, Element padre, String nombre) {
        Element e = doc.createElementNS(NS_CAC, "cac:" + nombre);   // ej. <cac:Party>
        padre.appendChild(e);                                       // lo mete dentro del padre
        return e;
    }

    // Crea <cbc:nombre currencyID="PEN">monto</cbc:nombre> con 2 decimales (formato SUNAT)
    private Element monto(Document doc, Element padre, String nombre, BigDecimal valor) {
        String texto = valor.setScale(2, RoundingMode.HALF_UP).toPlainString(); // 18 → "18.00"
        Element e = cbc(doc, padre, nombre, texto);                             // reutiliza cbc() para crear la etiqueta
        e.setAttribute("currencyID", "PEN");                                    // atributo moneda: soles
        return e;                                                               // lo devuelve por si hace falta
    }
               
        // lo devuelve para meterle hijos
    
}