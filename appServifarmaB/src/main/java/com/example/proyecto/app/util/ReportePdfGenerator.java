package com.example.proyecto.app.util;

import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;


/**
 * Generador de reportes en PDF con diseño profesional e institucional.
 * Utiliza iText (com.lowagie.text) para crear documentos con estilo corporativo.
 */
public final class ReportePdfGenerator {

    // ============================================================
    // CONSTANTES DE DISEÑO
    // ============================================================

    private static final Color COLOR_PRIMARIO = new Color(13, 148, 136);   // #0d9488
    private static final Color COLOR_SECUNDARIO = new Color(59, 130, 246); // #3b82f6
    private static final Color COLOR_ENCABEZADO = new Color(240, 253, 250);
    private static final Color COLOR_ALTERNO = new Color(248, 250, 252);
    private static final Color COLOR_TEXTO = new Color(30, 41, 59);
    private static final Color COLOR_TEXTO_CLARO = new Color(100, 116, 139);
    private static final Color COLOR_BORDE = new Color(203, 213, 225);
    private static final Color COLOR_POSITIVO = new Color(34, 197, 94);
    private static final Color COLOR_NEGATIVO = new Color(239, 68, 68);
    private static final Color COLOR_ADVERTENCIA = new Color(245, 158, 11);

    private static final Font FUENTE_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_PRIMARIO);
    private static final Font FUENTE_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_SECUNDARIO);
    private static final Font FUENTE_ENCABEZADO_TABLA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font FUENTE_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXTO);
    private static final Font FUENTE_NORMAL_NEGRITA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_TEXTO);
    private static final Font FUENTE_PEQUENA = FontFactory.getFont(FontFactory.HELVETICA, 7, COLOR_TEXTO_CLARO);
    private static final Font FUENTE_MONEDA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, COLOR_PRIMARIO);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReportePdfGenerator() {}

    // ============================================================
    // MÉTODO AUXILIAR: LOGO (si existe)
    // ============================================================

    private static void agregarLogo(Document document) {
        try {
            java.io.InputStream is = ReportePdfGenerator.class.getResourceAsStream("/logo.png");
            if (is != null) {
                Image logo = Image.getInstance(is.readAllBytes());
                logo.scaleToFit(100, 60);
                logo.setAlignment(Element.ALIGN_LEFT);
                document.add(logo);
            }
        } catch (Exception e) {
            // Si no hay logo, continuar sin él
        }
    }

    // ============================================================
    // MÉTODO AUXILIAR: PIE DE PÁGINA
    // ============================================================

    private static void agregarPiePagina(PdfWriter writer, String tituloReporte) {
        PdfPageEventHelper event = new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                Phrase footer = new Phrase(
                    tituloReporte + " | Generado: " + java.time.LocalDateTime.now().format(DATETIME_FORMATTER) +
                    " | Página " + writer.getPageNumber(),
                    FUENTE_PEQUENA
                );
                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, footer,
                    document.getPageSize().getWidth() / 2,
                    document.bottom() - 20, 0);
            }
        };
        writer.setPageEvent(event);
    }

    // ============================================================
    // MÉTODO AUXILIAR: CELDA CON COLOR
    // ============================================================

    private static PdfPCell crearCelda(String texto, Font fuente, Color fondo, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fuente));
        if (fondo != null) cell.setBackgroundColor(fondo);
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBorderColor(COLOR_BORDE);
        cell.setBorderWidth(0.5f);
        return cell;
    }

    private static PdfPCell crearCeldaMoneda(BigDecimal valor) {
        String texto = (valor != null) ? "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toString() : "S/ 0.00";
        return crearCelda(texto, FUENTE_MONEDA, null, Element.ALIGN_RIGHT);
    }

    // ============================================================
    // 1. REPORTE DIGEMIT
    // ============================================================

    public static byte[] generarPdfDigemit(ReporteDigemitResponse reporte) throws DocumentException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            agregarPiePagina(writer, "Reporte DIGEMID - " + reporte.getMes());
            document.open();

            agregarLogo(document);
            Paragraph titulo = new Paragraph("REPORTE DIGEMID", FUENTE_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph subtitulo = new Paragraph("Mes: " + reporte.getMes(), FUENTE_SUBTITULO);
            subtitulo.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitulo);

            Paragraph fechaGen = new Paragraph("Fecha de generación: " + 
                    reporte.getFechaGeneracion().format(DATE_FORMATTER), FUENTE_PEQUENA);
            fechaGen.setAlignment(Element.ALIGN_CENTER);
            document.add(fechaGen);
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(7);
            tabla.setWidthPercentage(100);
            tabla.setSpacingBefore(12);
            tabla.setSpacingAfter(12);
            tabla.setWidths(new float[]{1.2f, 2.2f, 1.8f, 2.0f, 1.2f, 1.5f, 1.0f});

            String[] headers = {"Código", "Producto", "Laboratorio", "Principio Activo", "Lote", "Vencimiento", "Stock"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FUENTE_ENCABEZADO_TABLA));
                cell.setBackgroundColor(COLOR_PRIMARIO);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                tabla.addCell(cell);
            }

            for (ReporteDigemitResponse.Item item : reporte.getItems()) {
                tabla.addCell(crearCelda(item.getCodigoProducto(), FUENTE_NORMAL, null, Element.ALIGN_CENTER));
                tabla.addCell(crearCelda(item.getNombreProducto(), FUENTE_NORMAL, null, Element.ALIGN_LEFT));
                tabla.addCell(crearCelda(item.getLaboratorio(), FUENTE_NORMAL, null, Element.ALIGN_LEFT));
                tabla.addCell(crearCelda(item.getPrincipioActivo(), FUENTE_NORMAL, null, Element.ALIGN_LEFT));
                tabla.addCell(crearCelda(item.getLote(), FUENTE_NORMAL, null, Element.ALIGN_CENTER));
                tabla.addCell(crearCelda(item.getFechaVencimiento().format(DATE_FORMATTER), FUENTE_NORMAL, null, Element.ALIGN_CENTER));
                tabla.addCell(crearCelda(String.valueOf(item.getCantidad()), FUENTE_NORMAL, null, Element.ALIGN_CENTER));
            }

            document.add(tabla);

            Paragraph nota = new Paragraph(
                "Este reporte ha sido generado automáticamente por el sistema ServiFarma. " +
                "La información contenida es de carácter oficial para fines de control sanitario.",
                FUENTE_PEQUENA
            );
            nota.setAlignment(Element.ALIGN_CENTER);
            document.add(nota);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            DocumentException de = new DocumentException("Error al generar PDF DIGEMIT: " + e.getMessage());
            de.initCause(e);
            throw de;
        }
    }

    // ============================================================
    // 2. REPORTE DE RENTABILIDAD
    // ============================================================

    public static byte[] generarPdfRentabilidad(ReporteRentabilidadResponse reporte) throws DocumentException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            agregarPiePagina(writer, "Reporte de Rentabilidad");
            document.open();

            agregarLogo(document);
            Paragraph titulo = new Paragraph("REPORTE DE RENTABILIDAD REAL", FUENTE_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph periodo = new Paragraph(
                "Período: " + reporte.getFechaInicio().format(DATE_FORMATTER) + " al " + 
                reporte.getFechaFin().format(DATE_FORMATTER), FUENTE_SUBTITULO
            );
            periodo.setAlignment(Element.ALIGN_CENTER);
            document.add(periodo);
            document.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(70);
            tabla.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.setSpacingBefore(12);
            tabla.setSpacingAfter(12);
            tabla.setWidths(new float[]{1.8f, 1.2f});

            PdfPCell header = new PdfPCell(new Phrase("RESUMEN DE RENTABILIDAD", FUENTE_SUBTITULO));
            header.setColspan(2);
            header.setBackgroundColor(COLOR_PRIMARIO);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8);
            tabla.addCell(header);

            agregarFilaResumen(tabla, "Ingresos Totales", reporte.getIngresosTotales(), COLOR_PRIMARIO);
            agregarFilaResumen(tabla, "Costo de Ventas", reporte.getCostoVentas(), COLOR_SECUNDARIO);
            agregarFilaResumen(tabla, "Mermas (pérdidas)", reporte.getMermas(), COLOR_ADVERTENCIA);
            agregarFilaResumen(tabla, "Margen Bruto", reporte.getMargenBruto(), COLOR_POSITIVO);
            agregarFilaResumen(tabla, "Margen Neto (real)", reporte.getMargenNeto(), COLOR_POSITIVO);

            document.add(tabla);
            document.add(new Paragraph(" "));

            if (reporte.getCategorias() != null && !reporte.getCategorias().isEmpty()) {
                Paragraph catTitulo = new Paragraph("RENTABILIDAD POR CATEGORÍA", FUENTE_SUBTITULO);
                catTitulo.setAlignment(Element.ALIGN_CENTER);
                document.add(catTitulo);

                PdfPTable catTabla = new PdfPTable(4);
                catTabla.setWidthPercentage(90);
                catTabla.setHorizontalAlignment(Element.ALIGN_CENTER);
                catTabla.setSpacingBefore(10);
                catTabla.setWidths(new float[]{2.0f, 1.2f, 1.2f, 1.2f});

                String[] catHeaders = {"Categoría", "Ingresos", "Costos", "Margen"};
                for (String h : catHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FUENTE_ENCABEZADO_TABLA));
                    cell.setBackgroundColor(COLOR_SECUNDARIO);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    catTabla.addCell(cell);
                }

                for (ReporteRentabilidadResponse.RentabilidadCategoria cat : reporte.getCategorias()) {
                    catTabla.addCell(crearCelda(cat.getCategoriaNombre(), FUENTE_NORMAL_NEGRITA, null, Element.ALIGN_LEFT));
                    catTabla.addCell(crearCeldaMoneda(cat.getIngresos()));
                    catTabla.addCell(crearCeldaMoneda(cat.getCostos()));
                    catTabla.addCell(crearCeldaMoneda(cat.getMargen()));
                }

                document.add(catTabla);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            DocumentException de = new DocumentException("Error al generar PDF de rentabilidad: " + e.getMessage());
            de.initCause(e);
            throw de;
        }
    }

    private static void agregarFilaResumen(PdfPTable tabla, String label, BigDecimal valor, Color color) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FUENTE_NORMAL_NEGRITA));
        labelCell.setPadding(5);
        labelCell.setBorderColor(COLOR_BORDE);
        labelCell.setBackgroundColor(COLOR_ALTERNO);
        tabla.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(
            (valor != null) ? "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toString() : "S/ 0.00",
            FUENTE_MONEDA
        ));
        valueCell.setPadding(5);
        valueCell.setBorderColor(COLOR_BORDE);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        if (color != null) {
            valueCell.setBackgroundColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 30));
        }
        tabla.addCell(valueCell);
    }

    // ============================================================
    // 3. ESTADÍSTICAS DE VENTAS
    // ============================================================

    public static byte[] generarPdfEstadisticas(EstadisticasVentasResponse estadisticas) throws DocumentException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            agregarPiePagina(writer, "Estadísticas de Ventas");
            document.open();

            agregarLogo(document);
            Paragraph titulo = new Paragraph("ESTADÍSTICAS DE VENTAS", FUENTE_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            Paragraph periodo = new Paragraph(
                "Período: " + estadisticas.getFechaInicio().format(DATETIME_FORMATTER) +
                " al " + estadisticas.getFechaFin().format(DATETIME_FORMATTER), FUENTE_SUBTITULO
            );
            periodo.setAlignment(Element.ALIGN_CENTER);
            document.add(periodo);

            if (estadisticas.getVariacionPorcentual() != null) {
                Paragraph variacion = new Paragraph(
                    "Variación vs período anterior: " +
                    estadisticas.getVariacionPorcentual().setScale(2, RoundingMode.HALF_UP) + "%",
                    FUENTE_NORMAL
                );
                variacion.setAlignment(Element.ALIGN_CENTER);
                document.add(variacion);
            }
            document.add(new Paragraph(" "));

            PdfPTable metricas = new PdfPTable(3);
            metricas.setWidthPercentage(80);
            metricas.setHorizontalAlignment(Element.ALIGN_CENTER);
            metricas.setSpacingBefore(10);
            metricas.setSpacingAfter(10);
            metricas.setWidths(new float[]{1.0f, 1.0f, 1.0f});

            PdfPCell cell1 = new PdfPCell(new Phrase("Total Ventas", FUENTE_ENCABEZADO_TABLA));
            cell1.setBackgroundColor(COLOR_PRIMARIO);
            cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
            metricas.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(new Phrase("Transacciones", FUENTE_ENCABEZADO_TABLA));
            cell2.setBackgroundColor(COLOR_PRIMARIO);
            cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
            metricas.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(new Phrase("Ticket Promedio", FUENTE_ENCABEZADO_TABLA));
            cell3.setBackgroundColor(COLOR_PRIMARIO);
            cell3.setHorizontalAlignment(Element.ALIGN_CENTER);
            metricas.addCell(cell3);

            metricas.addCell(crearCeldaMoneda(estadisticas.getTotalVentas()));
            metricas.addCell(crearCelda(
                estadisticas.getTotalTransacciones() != null ? String.valueOf(estadisticas.getTotalTransacciones()) : "0",
                FUENTE_NORMAL, null, Element.ALIGN_CENTER
            ));
            metricas.addCell(crearCeldaMoneda(estadisticas.getTicketPromedio()));

            document.add(metricas);
            document.add(new Paragraph(" "));

            if (estadisticas.getDistribucionMediosPago() != null && !estadisticas.getDistribucionMediosPago().isEmpty()) {
                Paragraph distTitle = new Paragraph("DISTRIBUCIÓN POR MEDIO DE PAGO", FUENTE_SUBTITULO);
                distTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(distTitle);

                PdfPTable dist = new PdfPTable(3);
                dist.setWidthPercentage(70);
                dist.setHorizontalAlignment(Element.ALIGN_CENTER);
                dist.setWidths(new float[]{1.5f, 1.0f, 1.0f});

                String[] distHeaders = {"Medio de Pago", "Monto", "Cantidad"};
                for (String h : distHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FUENTE_ENCABEZADO_TABLA));
                    cell.setBackgroundColor(COLOR_SECUNDARIO);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    dist.addCell(cell);
                }

                for (EstadisticasVentasResponse.DistribucionPago dp : estadisticas.getDistribucionMediosPago()) {
                    dist.addCell(crearCelda(dp.getMedioPago().name(), FUENTE_NORMAL, null, Element.ALIGN_LEFT));
                    dist.addCell(crearCeldaMoneda(dp.getTotal()));
                    dist.addCell(crearCelda(
                        String.valueOf(dp.getCantidadTransacciones()), FUENTE_NORMAL, null, Element.ALIGN_CENTER
                    ));
                }

                document.add(dist);
                document.add(new Paragraph(" "));
            }

            if (estadisticas.getProductosMasVendidos() != null && !estadisticas.getProductosMasVendidos().isEmpty()) {
                Paragraph topTitle = new Paragraph("TOP 10 PRODUCTOS MÁS VENDIDOS", FUENTE_SUBTITULO);
                topTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(topTitle);

                PdfPTable top = new PdfPTable(4);
                top.setWidthPercentage(90);
                top.setHorizontalAlignment(Element.ALIGN_CENTER);
                top.setWidths(new float[]{2.5f, 1.0f, 1.2f, 1.2f});

                String[] topHeaders = {"Producto", "Cantidad", "Total", "% del Total"};
                for (String h : topHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FUENTE_ENCABEZADO_TABLA));
                    cell.setBackgroundColor(COLOR_PRIMARIO);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    top.addCell(cell);
                }

                BigDecimal totalVentas = estadisticas.getTotalVentas();
                for (EstadisticasVentasResponse.ProductoTop pt : estadisticas.getProductosMasVendidos()) {
                    top.addCell(crearCelda(pt.getProductoNombre(), FUENTE_NORMAL, null, Element.ALIGN_LEFT));
                    top.addCell(crearCelda(
                        String.valueOf(pt.getCantidadVendida()), FUENTE_NORMAL, null, Element.ALIGN_CENTER
                    ));
                    top.addCell(crearCeldaMoneda(pt.getTotalFacturado()));

                    String porcentaje = "0%";
                    if (totalVentas != null && totalVentas.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal pct = pt.getTotalFacturado().divide(totalVentas, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        porcentaje = pct.setScale(1, RoundingMode.HALF_UP) + "%";
                    }
                    top.addCell(crearCelda(porcentaje, FUENTE_NORMAL_NEGRITA, null, Element.ALIGN_CENTER));
                }

                document.add(top);
                document.add(new Paragraph(" "));
            }

            if (estadisticas.getTendenciaDiaria() != null && !estadisticas.getTendenciaDiaria().isEmpty()) {
                Paragraph tendTitle = new Paragraph("TENDENCIA DIARIA (últimos 7 días)", FUENTE_SUBTITULO);
                tendTitle.setAlignment(Element.ALIGN_CENTER);
                document.add(tendTitle);

                PdfPTable tend = new PdfPTable(2);
                tend.setWidthPercentage(50);
                tend.setHorizontalAlignment(Element.ALIGN_CENTER);
                tend.setWidths(new float[]{1.0f, 1.0f});

                String[] tendHeaders = {"Fecha", "Total"};
                for (String h : tendHeaders) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, FUENTE_ENCABEZADO_TABLA));
                    cell.setBackgroundColor(COLOR_SECUNDARIO);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    tend.addCell(cell);
                }

                int count = 0;
                for (EstadisticasVentasResponse.TendenciaDiaria td : estadisticas.getTendenciaDiaria()) {
                    if (count >= 7) break;
                    tend.addCell(crearCelda(
                        td.getFecha().format(DATE_FORMATTER), FUENTE_NORMAL, null, Element.ALIGN_CENTER
                    ));
                    tend.addCell(crearCeldaMoneda(td.getTotal()));
                    count++;
                }

                document.add(tend);
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