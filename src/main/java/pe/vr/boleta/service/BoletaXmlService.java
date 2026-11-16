package pe.vr.boleta.service;

import org.springframework.stereotype.Service;

import pe.vr.boleta.model.Boleta;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;

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

    // Recibe una boleta y devuelve su XML (UBL 2.1) como texto
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

}