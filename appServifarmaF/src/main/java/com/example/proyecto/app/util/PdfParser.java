package com.example.proyecto.app.util;


import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidad para extraer texto y datos de archivos PDF. Útil para la importación
 * automática de facturas electrónicas de proveedores (RF17).
 * 
 * <p>
 * Ejemplo de uso:
 * </p>
 * 
 * <pre>
 * byte[] pdfBytes = ...;
 * String texto = PdfParser.extraerTexto(pdfBytes);
 * String ruc = PdfParser.extraerRUC(texto);
 * </pre>
 */
public final class PdfParser {

	private static final Pattern PATTERN_RUC = Pattern.compile("(?:RUC|R.U.C|RUC\\s*:?)\\s*(\\d{11})",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_FACTURA_NRO = Pattern.compile(
			"(?:FACTURA|BOLETA|COMPROBANTE)\\s*(?:N[°º]|NRO|NÚMERO)\\s*[:.]?\\s*([A-Z0-9-]+)",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_TOTAL = Pattern.compile(
			"(?:TOTAL|IMPORTE TOTAL|MONTO TOTAL)\\s*[:.]?\\s*S?/?\\s*(\\d+[.,]?\\d{0,2})", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_FECHA = Pattern.compile(
			"(?:FECHA|FECHA EMISIÓN)\\s*[:.]?\\s*(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})", Pattern.CASE_INSENSITIVE);
	private static final Pattern PATTERN_PROVEEDOR = Pattern
			.compile("(?:PROVEEDOR|VENDEDOR|EMISOR)\\s*[:.]?\\s*([\\w\\s.,-]{3,100})", Pattern.CASE_INSENSITIVE);

	private PdfParser() {
		// Constructor privado para evitar instanciación
	}

	// ==============================
	// EXTRACCIÓN DE TEXTO
	// ==============================

	/**
	 * Extrae todo el texto de un archivo PDF.
	 *
	 * @param pdfBytes Contenido del PDF en bytes.
	 * @return Texto completo extraído del PDF.
	 * @throws IllegalArgumentException Si el contenido es nulo o vacío.
	 * @throws IOException              Si ocurre un error al leer el PDF.
	 */
	public static String extraerTexto(byte[] pdfBytes) throws IOException {
	    if (pdfBytes == null || pdfBytes.length == 0) {
	        throw new IllegalArgumentException("El contenido del PDF no puede ser nulo o vacío.");
	    }

	    try (ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);
	         PdfReader reader = new PdfReader(bais)) {

	        int pages = reader.getNumberOfPages();
	        StringBuilder text = new StringBuilder();
	        PdfTextExtractor extractor = new PdfTextExtractor(reader);

	        for (int page = 1; page <= pages; page++) {
	            String pageText = extractor.getTextFromPage(page);
	            text.append(pageText).append("\n");
	        }

	        return text.toString();
	    }
	}

	/**
	 * Extrae el texto página por página de un archivo PDF.
	 *
	 * @param pdfBytes Contenido del PDF en bytes.
	 * @return Lista de textos, donde cada elemento corresponde a una página.
	 * @throws IOException Si ocurre un error al leer el PDF.
	 */
	public static List<String> extraerTextoPorPagina(byte[] pdfBytes) throws IOException {
		if (pdfBytes == null || pdfBytes.length == 0) {
			throw new IllegalArgumentException("El contenido del PDF no puede ser nulo o vacío.");
		}

		List<String> paginas = new ArrayList<>();

		try (ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes); PdfReader reader = new PdfReader(bais)) {

			int pages = reader.getNumberOfPages();
			PdfTextExtractor extractor = new PdfTextExtractor(reader);
			for (int page = 1; page <= pages; page++) {
				String pageText = extractor.getTextFromPage(page);
				paginas.add(pageText);
			}
		}

		return paginas;
	}

	// ==============================
	// EXTRACCIÓN DE DATOS ESPECÍFICOS DE FACTURA
	// ==============================

	/**
	 * Busca el RUC del proveedor en el texto extraído del PDF.
	 *
	 * @param texto Texto completo del PDF.
	 * @return RUC de 11 dígitos si se encuentra, null en caso contrario.
	 */
	public static String extraerRUC(String texto) {
		return extraerPatron(texto, PATTERN_RUC);
	}

	/**
	 * Busca el número de factura o comprobante en el texto.
	 *
	 * @param texto Texto completo del PDF.
	 * @return Número de factura si se encuentra, null en caso contrario.
	 */
	public static String extraerNumeroFactura(String texto) {
		return extraerPatron(texto, PATTERN_FACTURA_NRO);
	}

	/**
	 * Busca el monto total de la factura en el texto.
	 *
	 * @param texto Texto completo del PDF.
	 * @return Monto total como String (formato "1234.56"), null si no se encuentra.
	 */
	public static String extraerTotal(String texto) {
		String match = extraerPatron(texto, PATTERN_TOTAL);
		if (match != null) {
			// Reemplazar coma por punto para estandarizar el formato decimal
			return match.replace(",", ".");
		}
		return null;
	}

	/**
	 * Busca la fecha de emisión en el texto.
	 *
	 * @param texto Texto completo del PDF.
	 * @return Fecha en formato dd/MM/yyyy o dd-MM-yyyy, null si no se encuentra.
	 */
	public static String extraerFechaEmision(String texto) {
		return extraerPatron(texto, PATTERN_FECHA);
	}

	/**
	 * Busca el nombre del proveedor/emisor en el texto.
	 *
	 * @param texto Texto completo del PDF.
	 * @return Nombre del proveedor si se encuentra, null en caso contrario.
	 */
	public static String extraerProveedor(String texto) {
		return extraerPatron(texto, PATTERN_PROVEEDOR);
	}

	/**
	 * Método genérico para extraer una coincidencia usando una expresión regular.
	 *
	 * @param texto   Texto completo.
	 * @param pattern Patrón a buscar.
	 * @return Primer grupo de captura si hay coincidencia, null en caso contrario.
	 */
	private static String extraerPatron(String texto, Pattern pattern) {
		if (texto == null || texto.isEmpty()) {
			return null;
		}
		Matcher matcher = pattern.matcher(texto);
		if (matcher.find()) {
			return matcher.group(1).trim();
		}
		return null;
	}

	// ==============================
	// MÉTODO DE EXTENSIÓN PARA IMPORTACIÓN COMPLETA
	// ==============================

	/**
	 * Extrae todos los datos relevantes de una factura electrónica en un solo paso.
	 * 
	 * @param pdfBytes Contenido del PDF en bytes.
	 * @return Un objeto FacturaData con los campos extraídos.
	 * @throws IOException Si ocurre un error al leer el PDF.
	 */
	public static FacturaData extraerDatosFactura(byte[] pdfBytes) throws IOException {
		String texto = extraerTexto(pdfBytes);
		FacturaData data = new FacturaData();
		data.setRucProveedor(extraerRUC(texto));
		data.setNumeroFactura(extraerNumeroFactura(texto));
		data.setTotal(extraerTotal(texto));
		data.setFechaEmision(extraerFechaEmision(texto));
		data.setProveedor(extraerProveedor(texto));
		data.setTextoCompleto(texto);
		return data;
	}

	/**
	 * Clase contenedora para los datos extraídos de una factura. Útil para
	 * transferir los datos a la capa de servicio.
	 */
	public static class FacturaData {
		private String rucProveedor;
		private String numeroFactura;
		private String total;
		private String fechaEmision;
		private String proveedor;
		private String textoCompleto;

		// Getters y Setters
		public String getRucProveedor() {
			return rucProveedor;
		}

		public void setRucProveedor(String rucProveedor) {
			this.rucProveedor = rucProveedor;
		}

		public String getNumeroFactura() {
			return numeroFactura;
		}

		public void setNumeroFactura(String numeroFactura) {
			this.numeroFactura = numeroFactura;
		}

		public String getTotal() {
			return total;
		}

		public void setTotal(String total) {
			this.total = total;
		}

		public String getFechaEmision() {
			return fechaEmision;
		}

		public void setFechaEmision(String fechaEmision) {
			this.fechaEmision = fechaEmision;
		}

		public String getProveedor() {
			return proveedor;
		}

		public void setProveedor(String proveedor) {
			this.proveedor = proveedor;
		}

		public String getTextoCompleto() {
			return textoCompleto;
		}

		public void setTextoCompleto(String textoCompleto) {
			this.textoCompleto = textoCompleto;
		}

		@Override
		public String toString() {
			return "FacturaData{" + "rucProveedor='" + rucProveedor + '\'' + ", numeroFactura='" + numeroFactura + '\''
					+ ", total='" + total + '\'' + ", fechaEmision='" + fechaEmision + '\'' + ", proveedor='"
					+ proveedor + '\'' + '}';
		}
	}
}