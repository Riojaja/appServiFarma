package com.example.proyecto.app.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para parsear y extraer datos de archivos XML (facturas electrónicas, comprobantes).
 * Soporta búsqueda de tags ignorando namespaces, lo que lo hace compatible con
 * formatos UBL, SUNAT y XMLs genéricos.
 * 
 * <p>Ejemplo de uso:</p>
 * <pre>
 * byte[] xmlBytes = ...;
 * Document doc = XmlParser.parseXml(xmlBytes);
 * String ruc = XmlParser.findTextByLocalName(doc, "ID", 0);
 * </pre>
 */
public final class XmlParser {

    private XmlParser() {
        // Constructor privado para evitar instanciación
    }

    // ==============================
    // PARSEO DE XML
    // ==============================

    /**
     * Parsea un arreglo de bytes en un objeto Document DOM.
     *
     * @param xmlBytes Contenido del XML en bytes.
     * @return Document DOM del XML.
     * @throws ParserConfigurationException Si hay un error de configuración del parser.
     * @throws IOException Si hay un error de lectura.
     * @throws SAXException Si el XML tiene errores de sintaxis.
     */
    public static Document parseXml(byte[] xmlBytes) throws ParserConfigurationException, IOException, SAXException {
        if (xmlBytes == null || xmlBytes.length == 0) {
            throw new IllegalArgumentException("El contenido del XML no puede ser nulo o vacío.");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);  // Ignorar namespaces para facilitar la búsqueda
        DocumentBuilder builder = factory.newDocumentBuilder();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(xmlBytes)) {
            return builder.parse(bais);
        }
    }

    /**
     * Parsea un String XML en un objeto Document DOM.
     *
     * @param xmlStr Contenido del XML como String.
     * @return Document DOM del XML.
     * @throws ParserConfigurationException Si hay un error de configuración del parser.
     * @throws IOException Si hay un error de lectura.
     * @throws SAXException Si el XML tiene errores de sintaxis.
     */
    public static Document parseXml(String xmlStr) throws ParserConfigurationException, IOException, SAXException {
        if (xmlStr == null || xmlStr.isBlank()) {
            throw new IllegalArgumentException("El contenido del XML no puede ser nulo o vacío.");
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        try (StringReader reader = new StringReader(xmlStr)) {
            return builder.parse(new InputSource(reader));
        }
    }

    // ==============================
    // BÚSQUEDA DE TEXTO EN EL XML
    // ==============================

    /**
     * Busca recursivamente el primer nodo con el nombre local especificado
     * y devuelve su contenido de texto.
     *
     * @param node      Nodo raíz desde donde comenzar la búsqueda.
     * @param localName Nombre local de la etiqueta a buscar (ej. "ID", "RUC", "PayableAmount").
     * @return El contenido de texto del primer nodo encontrado, o null si no existe.
     */
    public static String findTextByLocalName(Node node, String localName) {
        if (node == null) return null;

        // Si el nodo actual coincide, devolver su texto
        if (localName.equals(node.getLocalName())) {
            return node.getTextContent() != null ? node.getTextContent().trim() : null;
        }

        // Recorrer nodos hijos
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            String result = findTextByLocalName(childNodes.item(i), localName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Busca recursivamente todos los nodos con el nombre local especificado
     * y devuelve una lista con sus contenidos de texto (sin espacios).
     *
     * @param node      Nodo raíz desde donde comenzar la búsqueda.
     * @param localName Nombre local de la etiqueta a buscar.
     * @return Lista de contenidos de texto de los nodos encontrados.
     */
    public static List<String> findAllTextByLocalName(Node node, String localName) {
        List<String> results = new ArrayList<>();
        collectTextByLocalName(node, localName, results);
        return results;
    }

    private static void collectTextByLocalName(Node node, String localName, List<String> results) {
        if (node == null) return;

        if (localName.equals(node.getLocalName())) {
            String text = node.getTextContent();
            if (text != null && !text.isBlank()) {
                results.add(text.trim());
            }
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            collectTextByLocalName(childNodes.item(i), localName, results);
        }
    }

    // ==============================
    // EXTRACCIÓN DE DATOS DE FACTURA ELECTRÓNICA (SUNAT / UBL)
    // ==============================

    /**
     * Extrae los datos clave de una factura electrónica en formato UBL.
     * Utiliza nombres locales comunes en facturas SUNAT y otros estándares.
     *
     * @param doc Document DOM del XML.
     * @return Objeto FacturaData con los campos extraídos.
     */
    public static FacturaData extraerDatosFactura(Document doc) {
        FacturaData data = new FacturaData();

        // 1. RUC del proveedor (Emisor)
        // Formato SUNAT: cac:AccountingSupplierParty -> cac:Party -> cac:PartyIdentification -> cbc:ID
        // Buscamos "ID" que esté dentro del contexto de proveedor, pero por simplicidad,
        // tomamos el primer "ID" que no sea el de la factura. Alternativa: buscar "RUC" directamente.
        // Muchos XMLs tienen un tag <ruc> o <RUC> o <cbc:ID> con el RUC del emisor.
        // Usamos búsqueda recursiva.
        String ruc = findTextByLocalName(doc, "ID");
        // Si no es RUC válido, puede ser el ID de la factura.
        // Para facturas SUNAT, la estructura tiene dos IDs: uno para la factura (Invoice ID) y otro para el proveedor (Supplier ID).
        // Dado que la búsqueda devuelve el primero, suele ser el ID de la factura.
        // Mejor buscar un patrón específico: <cbc:ID> que sea hijo de AccountingSupplierParty.
        // Como no usamos XPath, buscamos una alternativa: Podemos buscar el RUC por el nombre "ruc" o "RUC".
        // Intentamos buscar tags "RUC", "ruc", "NUMRUC".
        String rucAlt = findTextByLocalName(doc, "RUC");
        if (rucAlt != null && rucAlt.length() == 11) {
            data.setRucProveedor(rucAlt);
        } else {
            // Si no, asumimos que el primer ID encontrado es el de la factura, el segundo el del proveedor.
            // Usamos findAllTextByLocalName para obtener todos los IDs.
            List<String> ids = findAllTextByLocalName(doc, "ID");
            if (ids.size() >= 2) {
                // El segundo ID suele ser el RUC del proveedor en UBL 2.1
                String possibleRuc = ids.get(1);
                if (possibleRuc.length() == 11) {
                    data.setRucProveedor(possibleRuc);
                } else {
                    data.setRucProveedor(ruc);
                }
            } else if (!ids.isEmpty()) {
                data.setRucProveedor(ids.get(0));
            }
        }

        // 2. Número de factura
        // Buscar "ID" específico de la factura o "InvoiceNumber".
        // En UBL, la factura tiene un <cbc:ID> en la raíz.
        String numFactura = findTextByLocalName(doc, "InvoiceNumber");
        if (numFactura == null) {
            numFactura = findTextByLocalName(doc, "ID");
        }
        data.setNumeroFactura(numFactura);

        // 3. Total
        // Buscar "PayableAmount" (Monto total a pagar)
        String total = findTextByLocalName(doc, "PayableAmount");
        if (total == null) {
            total = findTextByLocalName(doc, "Total");
        }
        if (total != null) {
            // Reemplazar coma por punto y limpiar espacios
            total = total.replace(",", ".").trim();
        }
        data.setTotal(total);

        // 4. Fecha de emisión
        // Buscar "IssueDate" (Fecha de emisión) y "IssueTime" (Hora)
        String fecha = findTextByLocalName(doc, "IssueDate");
        if (fecha == null) {
            fecha = findTextByLocalName(doc, "FechaEmision");
        }
        if (fecha != null) {
            data.setFechaEmision(fecha.trim());
        }

        // 5. Nombre del proveedor
        // Buscar "RegistrationName" (Nombre registrado) o "Name" del proveedor
        String proveedor = findTextByLocalName(doc, "RegistrationName");
        if (proveedor == null) {
            proveedor = findTextByLocalName(doc, "Name");
        }
        if (proveedor == null) {
            proveedor = findTextByLocalName(doc, "RazonSocial");
        }
        data.setProveedor(proveedor);

        // 6. Lista de items (productos/lotes) - opcional
        List<String> itemDescriptions = findAllTextByLocalName(doc, "Description");
        List<String> itemQuantities = findAllTextByLocalName(doc, "Quantity");
        List<String> itemPrices = findAllTextByLocalName(doc, "PriceAmount");
        List<String> itemTotalAmounts = findAllTextByLocalName(doc, "LineExtensionAmount");

        List<ItemData> items = new ArrayList<>();
        int maxItems = Math.max(itemDescriptions.size(), Math.max(itemQuantities.size(), itemPrices.size()));
        for (int i = 0; i < maxItems; i++) {
            ItemData item = new ItemData();
            if (i < itemDescriptions.size()) item.setDescripcion(itemDescriptions.get(i));
            if (i < itemQuantities.size()) item.setCantidad(itemQuantities.get(i));
            if (i < itemPrices.size()) item.setPrecioUnitario(itemPrices.get(i));
            if (i < itemTotalAmounts.size()) item.setTotalItem(itemTotalAmounts.get(i));
            items.add(item);
        }
        data.setItems(items);

        return data;
    }

    // ==============================
    // CLASES INTERNAS (Datos de Factura e Item)
    // ==============================

    /**
     * Clase contenedora para los datos extraídos de una factura electrónica XML.
     */
    public static class FacturaData {
        private String rucProveedor;
        private String numeroFactura;
        private String total;
        private String fechaEmision;
        private String proveedor;
        private List<ItemData> items = new ArrayList<>();

        public String getRucProveedor() { return rucProveedor; }
        public void setRucProveedor(String rucProveedor) { this.rucProveedor = rucProveedor; }
        public String getNumeroFactura() { return numeroFactura; }
        public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }
        public String getTotal() { return total; }
        public void setTotal(String total) { this.total = total; }
        public String getFechaEmision() { return fechaEmision; }
        public void setFechaEmision(String fechaEmision) { this.fechaEmision = fechaEmision; }
        public String getProveedor() { return proveedor; }
        public void setProveedor(String proveedor) { this.proveedor = proveedor; }
        public List<ItemData> getItems() { return items; }
        public void setItems(List<ItemData> items) { this.items = items; }

        public BigDecimal getTotalAsBigDecimal() {
            if (total == null) return BigDecimal.ZERO;
            try {
                return new BigDecimal(total);
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }

        @Override
        public String toString() {
            return "FacturaData{" +
                    "rucProveedor='" + rucProveedor + '\'' +
                    ", numeroFactura='" + numeroFactura + '\'' +
                    ", total='" + total + '\'' +
                    ", fechaEmision='" + fechaEmision + '\'' +
                    ", proveedor='" + proveedor + '\'' +
                    ", items=" + items.size() +
                    '}';
        }
    }

    /**
     * Clase contenedora para un item (producto) dentro de la factura.
     */
    public static class ItemData {
        private String descripcion;
        private String cantidad;
        private String precioUnitario;
        private String totalItem;

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
        public String getCantidad() { return cantidad; }
        public void setCantidad(String cantidad) { this.cantidad = cantidad; }
        public String getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(String precioUnitario) { this.precioUnitario = precioUnitario; }
        public String getTotalItem() { return totalItem; }
        public void setTotalItem(String totalItem) { this.totalItem = totalItem; }

        @Override
        public String toString() {
            return "ItemData{" +
                    "descripcion='" + descripcion + '\'' +
                    ", cantidad='" + cantidad + '\'' +
                    ", precioUnitario='" + precioUnitario + '\'' +
                    ", totalItem='" + totalItem + '\'' +
                    '}';
        }
    }
}