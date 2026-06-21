package com.example.proyecto.app.util;


import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.awt.Color;

public final class ReportePdfGenerator {

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
	private static final Font SUBTITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
	private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 9);

	private ReportePdfGenerator() {
	}

	// ==============================
	// REPORTE DIGEMIT
	// ==============================

	public static byte[] generarPdfDigemit(ReporteDigemitResponse reporte) throws DocumentException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4.rotate());
			PdfWriter.getInstance(document, baos);
			document.open();

			Paragraph title = new Paragraph("REPORTE DIGEMIT - " + reporte.getMes(), TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph date = new Paragraph(
					"Fecha de generación: " + reporte.getFechaGeneracion().format(DATE_FORMATTER), NORMAL_FONT);
			date.setAlignment(Element.ALIGN_CENTER);
			document.add(date);
			document.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(7);
			table.setWidthPercentage(100);
			table.setSpacingBefore(10f);
			table.setSpacingAfter(10f);

			String[] headers = { "Código", "Producto", "Laboratorio", "Principio Activo", "Lote", "Vencimiento",
					"Cantidad" };
			for (String header : headers) {
				PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
				cell.setBackgroundColor(Color.LIGHT_GRAY);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				table.addCell(cell);
			}

			for (ReporteDigemitResponse.Item item : reporte.getItems()) {
				table.addCell(new Phrase(item.getCodigoProducto(), NORMAL_FONT));
				table.addCell(new Phrase(item.getNombreProducto(), NORMAL_FONT));
				table.addCell(new Phrase(item.getLaboratorio(), NORMAL_FONT));
				table.addCell(new Phrase(item.getPrincipioActivo(), NORMAL_FONT));
				table.addCell(new Phrase(item.getLote(), NORMAL_FONT));
				table.addCell(new Phrase(item.getFechaVencimiento().format(DATE_FORMATTER), NORMAL_FONT));
				table.addCell(new Phrase(String.valueOf(item.getCantidad()), NORMAL_FONT));
			}

			document.add(table);
			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			DocumentException de = new DocumentException("Error al generar PDF DIGEMIT: " + e.getMessage());
			de.initCause(e);
			throw de;
		}
	}

	// ==============================
	// REPORTE DE RENTABILIDAD
	// ==============================

	public static byte[] generarPdfRentabilidad(ReporteRentabilidadResponse reporte) throws DocumentException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, baos);
			document.open();

			Paragraph title = new Paragraph("REPORTE DE RENTABILIDAD REAL", TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			Paragraph periodo = new Paragraph("Período: " + reporte.getFechaInicio().format(DATE_FORMATTER) + " al "
					+ reporte.getFechaFin().format(DATE_FORMATTER), NORMAL_FONT);
			periodo.setAlignment(Element.ALIGN_CENTER);
			document.add(periodo);
			document.add(new Paragraph(" "));

			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(80);
			table.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.setSpacingBefore(10f);

			PdfPCell headerCell = new PdfPCell(new Phrase("RESUMEN GENERAL", SUBTITLE_FONT));
			headerCell.setColspan(2);
			headerCell.setBackgroundColor(Color.LIGHT_GRAY);
			headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(headerCell);

			addResumenRow(table, "Ingresos Totales", reporte.getIngresosTotales());
			addResumenRow(table, "Costo de Ventas", reporte.getCostoVentas());
			addResumenRow(table, "Mermas (pérdidas)", reporte.getMermas());
			addResumenRow(table, "Margen Bruto", reporte.getMargenBruto());
			addResumenRow(table, "Margen Neto (real)", reporte.getMargenNeto());

			document.add(table);
			document.add(new Paragraph(" "));

			if (reporte.getCategorias() != null && !reporte.getCategorias().isEmpty()) {
				// ... (desglose por categoría) ...
			}

			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			DocumentException de = new DocumentException("Error al generar PDF de rentabilidad: " + e.getMessage());
			de.initCause(e);
			throw de;
		}
	}

	// ... resto de métodos (addResumenRow, addTransaccionRow, formatCurrency) ...

	private static void addResumenRow(PdfPTable table, String label, BigDecimal value) {
		table.addCell(new Phrase(label, NORMAL_FONT));
		table.addCell(new Phrase(formatCurrency(value), NORMAL_FONT));
	}

	private static void addTransaccionRow(PdfPTable table, String label, Long value) {
		table.addCell(new Phrase(label, NORMAL_FONT));
		table.addCell(new Phrase(value != null ? String.valueOf(value) : "0", NORMAL_FONT));
	}

	private static String formatCurrency(BigDecimal value) {
		if (value == null)
			return "S/ 0.00";
		return "S/ " + value.setScale(2, java.math.RoundingMode.HALF_UP).toString();
	}

	/**
	 * Genera un archivo PDF con el reporte de estadísticas de ventas.
	 *
	 * @param estadisticas Estadísticas de ventas.
	 * @return byte[] con el contenido del archivo PDF.
	 * @throws DocumentException Si ocurre un error al generar el PDF.
	 */
	public static byte[] generarPdfEstadisticas(EstadisticasVentasResponse estadisticas) throws DocumentException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Document document = new Document(PageSize.A4);
			PdfWriter.getInstance(document, baos);
			document.open();

			// Título
			Paragraph title = new Paragraph("ESTADÍSTICAS DE VENTAS", TITLE_FONT);
			title.setAlignment(Element.ALIGN_CENTER);
			document.add(title);

			// Período
			Paragraph periodo = new Paragraph("Período: " + estadisticas.getFechaInicio().format(DATETIME_FORMATTER)
					+ " al " + estadisticas.getFechaFin().format(DATETIME_FORMATTER), NORMAL_FONT);
			periodo.setAlignment(Element.ALIGN_CENTER);
			document.add(periodo);

			// Variación
			if (estadisticas.getVariacionPorcentual() != null) {
				Paragraph var = new Paragraph("Variación vs período anterior: "
						+ estadisticas.getVariacionPorcentual().setScale(2, java.math.RoundingMode.HALF_UP) + "%",
						NORMAL_FONT);
				var.setAlignment(Element.ALIGN_CENTER);
				document.add(var);
			}
			document.add(new Paragraph(" "));

			// Tabla de métricas principales
			PdfPTable metricTable = new PdfPTable(2);
			metricTable.setWidthPercentage(70);
			metricTable.setHorizontalAlignment(Element.ALIGN_CENTER);
			metricTable.setSpacingBefore(10f);

			PdfPCell metricHeader = new PdfPCell(new Phrase("MÉTRICAS PRINCIPALES", SUBTITLE_FONT));
			metricHeader.setColspan(2);
			metricHeader.setBackgroundColor(Color.LIGHT_GRAY);
			metricHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
			metricTable.addCell(metricHeader);

			addResumenRow(metricTable, "Total Ventas", estadisticas.getTotalVentas());
			addTransaccionRow(metricTable, "Total Transacciones", estadisticas.getTotalTransacciones());
			addResumenRow(metricTable, "Ticket Promedio", estadisticas.getTicketPromedio());

			document.add(metricTable);
			document.add(new Paragraph(" "));

			// Distribución por medio de pago
			if (estadisticas.getDistribucionMediosPago() != null
					&& !estadisticas.getDistribucionMediosPago().isEmpty()) {
				Paragraph distTitle = new Paragraph("DISTRIBUCIÓN POR MEDIO DE PAGO", SUBTITLE_FONT);
				distTitle.setAlignment(Element.ALIGN_CENTER);
				document.add(distTitle);

				PdfPTable distTable = new PdfPTable(3);
				distTable.setWidthPercentage(70);
				distTable.setHorizontalAlignment(Element.ALIGN_CENTER);
				distTable.setSpacingBefore(10f);

				String[] distHeaders = { "Medio de Pago", "Total", "Transacciones" };
				for (String h : distHeaders) {
					PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
					cell.setBackgroundColor(Color.LIGHT_GRAY);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					distTable.addCell(cell);
				}

				for (EstadisticasVentasResponse.DistribucionPago dp : estadisticas.getDistribucionMediosPago()) {
					distTable.addCell(new Phrase(dp.getMedioPago().name(), NORMAL_FONT));
					distTable.addCell(new Phrase(formatCurrency(dp.getTotal()), NORMAL_FONT));
					distTable.addCell(new Phrase(String.valueOf(dp.getCantidadTransacciones()), NORMAL_FONT));
				}

				document.add(distTable);
				document.add(new Paragraph(" "));
			}

			// Productos más vendidos
			if (estadisticas.getProductosMasVendidos() != null && !estadisticas.getProductosMasVendidos().isEmpty()) {
				Paragraph topTitle = new Paragraph("PRODUCTOS MÁS VENDIDOS", SUBTITLE_FONT);
				topTitle.setAlignment(Element.ALIGN_CENTER);
				document.add(topTitle);

				PdfPTable topTable = new PdfPTable(4);
				topTable.setWidthPercentage(90);
				topTable.setHorizontalAlignment(Element.ALIGN_CENTER);
				topTable.setSpacingBefore(10f);

				String[] topHeaders = { "Producto", "Cantidad", "Total Facturado", "% del Total" };
				for (String h : topHeaders) {
					PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
					cell.setBackgroundColor(Color.LIGHT_GRAY);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					topTable.addCell(cell);
				}

				BigDecimal totalVentas = estadisticas.getTotalVentas();
				for (EstadisticasVentasResponse.ProductoTop pt : estadisticas.getProductosMasVendidos()) {
					topTable.addCell(new Phrase(pt.getProductoNombre(), NORMAL_FONT));
					topTable.addCell(new Phrase(String.valueOf(pt.getCantidadVendida()), NORMAL_FONT));
					topTable.addCell(new Phrase(formatCurrency(pt.getTotalFacturado()), NORMAL_FONT));

					String porcentaje = "0%";
					if (totalVentas != null && totalVentas.compareTo(BigDecimal.ZERO) > 0) {
						BigDecimal pct = pt.getTotalFacturado().divide(totalVentas, 4, java.math.RoundingMode.HALF_UP)
								.multiply(BigDecimal.valueOf(100));
						porcentaje = pct.setScale(1, java.math.RoundingMode.HALF_UP) + "%";
					}
					topTable.addCell(new Phrase(porcentaje, NORMAL_FONT));
				}

				document.add(topTable);
				document.add(new Paragraph(" "));
			}

			// Tendencia diaria (últimos 10 días)
			if (estadisticas.getTendenciaDiaria() != null && !estadisticas.getTendenciaDiaria().isEmpty()) {
				Paragraph tendTitle = new Paragraph("TENDENCIA DIARIA (últimos 10 días)", SUBTITLE_FONT);
				tendTitle.setAlignment(Element.ALIGN_CENTER);
				document.add(tendTitle);

				PdfPTable tendTable = new PdfPTable(2);
				tendTable.setWidthPercentage(60);
				tendTable.setHorizontalAlignment(Element.ALIGN_CENTER);
				tendTable.setSpacingBefore(10f);

				String[] tendHeaders = { "Fecha", "Total" };
				for (String h : tendHeaders) {
					PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
					cell.setBackgroundColor(Color.LIGHT_GRAY);
					cell.setHorizontalAlignment(Element.ALIGN_CENTER);
					tendTable.addCell(cell);
				}

				int count = 0;
				for (EstadisticasVentasResponse.TendenciaDiaria td : estadisticas.getTendenciaDiaria()) {
					if (count >= 10)
						break;
					tendTable.addCell(new Phrase(td.getFecha().format(DATE_FORMATTER), NORMAL_FONT));
					tendTable.addCell(new Phrase(formatCurrency(td.getTotal()), NORMAL_FONT));
					count++;
				}

				document.add(tendTable);
			}

			document.close();
			return baos.toByteArray();
		} catch (Exception e) {
			DocumentException de = new DocumentException("Error al generar PDF de estadísticas: " + e.getMessage());
			de.initCause(e);
			throw de;
		}
	}
}