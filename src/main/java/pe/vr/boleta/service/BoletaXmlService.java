package pe.vr.boleta.service;

import org.springframework.stereotype.Service;

import pe.vr.boleta.model.Boleta;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

}