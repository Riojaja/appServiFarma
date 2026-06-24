package com.example.proyecto.app.util;

import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;
import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para generar reportes en formato Excel (.xlsx) utilizando Apache POI.
 * Proporciona métodos para exportar reportes DIGEMIT, rentabilidad y estadísticas de ventas.
 */
public final class ReporteExcelGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private ReporteExcelGenerator() {
        // Constructor privado para evitar instanciación
    }

    // ==============================
    // REPORTE DIGEMIT
    // ==============================

    /**
     * Genera un archivo Excel con el reporte DIGEMIT.
     *
     * @param reporte Reporte DIGEMIT generado.
     * @return byte[] con el contenido del archivo Excel.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static byte[] generarExcelDigemit(ReporteDigemitResponse reporte) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Reporte DIGEMIT");
            int rowNum = 0;

            // Estilos
            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle integerStyle = crearEstiloEntero(workbook);

            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DIGEMIT - " + reporte.getMes());
            titleCell.setCellStyle(crearEstiloTitulo(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            // Fecha de generación
            Row fechaRow = sheet.createRow(rowNum++);
            fechaRow.createCell(0).setCellValue("Fecha de generación: " + reporte.getFechaGeneracion().format(DATE_FORMATTER));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 6));

            rowNum++; // Línea en blanco

            // Encabezados
            String[] headers = {"Código", "Producto", "Laboratorio", "Principio Activo", "Lote", "Vencimiento", "Cantidad"};
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            for (ReporteDigemitResponse.Item item : reporte.getItems()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(item.getCodigoProducto());
                row.createCell(1).setCellValue(item.getNombreProducto());
                row.createCell(2).setCellValue(item.getLaboratorio());
                row.createCell(3).setCellValue(item.getPrincipioActivo());
                row.createCell(4).setCellValue(item.getLote());

                Cell fechaCell = row.createCell(5);
                fechaCell.setCellValue(item.getFechaVencimiento().format(DATE_FORMATTER));
                fechaCell.setCellStyle(dateStyle);

                Cell cantCell = row.createCell(6);
                cantCell.setCellValue(item.getCantidad());
                cantCell.setCellStyle(integerStyle);
            }

            // Autoajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ==============================
    // REPORTE DE RENTABILIDAD
    // ==============================

    /**
     * Genera un archivo Excel con el reporte de rentabilidad.
     *
     * @param reporte Reporte de rentabilidad generado.
     * @return byte[] con el contenido del archivo Excel.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static byte[] generarExcelRentabilidad(ReporteRentabilidadResponse reporte) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Rentabilidad");
            int rowNum = 0;

            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);
            //CellStyle negativeStyle = crearEstiloNegativo(workbook);

            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("REPORTE DE RENTABILIDAD REAL");
            titleCell.setCellStyle(crearEstiloTitulo(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            // Período
            Row periodoRow = sheet.createRow(rowNum++);
            periodoRow.createCell(0).setCellValue("Período: " +
                    reporte.getFechaInicio().format(DATE_FORMATTER) + " al " +
                    reporte.getFechaFin().format(DATE_FORMATTER));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 3));

            rowNum++; // Línea en blanco

            // Resumen
            Row resumenRow = sheet.createRow(rowNum++);
            resumenRow.createCell(0).setCellValue("RESUMEN GENERAL");
            resumenRow.getCell(0).setCellStyle(crearEstiloSubTitulo(workbook));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            // Datos del resumen
            String[][] resumenData = {
                    {"Ingresos Totales", formatCurrency(reporte.getIngresosTotales())},
                    {"Costo de Ventas", formatCurrency(reporte.getCostoVentas())},
                    {"Mermas (pérdidas)", formatCurrency(reporte.getMermas())},
                    {"Margen Bruto", formatCurrency(reporte.getMargenBruto())},
                    {"Margen Neto (real)", formatCurrency(reporte.getMargenNeto())}
            };

            for (String[] data : resumenData) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(data[0]);
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(data[1].replace("S/", "").trim());
                valueCell.setCellStyle(currencyStyle);
            }

            // Si hay categorías, agregar desglose
            if (reporte.getCategorias() != null && !reporte.getCategorias().isEmpty()) {
                rowNum++; // Línea en blanco

                Row catRow = sheet.createRow(rowNum++);
                catRow.createCell(0).setCellValue("DESGLOSE POR CATEGORÍA");
                catRow.getCell(0).setCellStyle(crearEstiloSubTitulo(workbook));
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

                // Encabezados categorías
                Row catHeader = sheet.createRow(rowNum++);
                catHeader.createCell(0).setCellValue("Categoría");
                catHeader.createCell(1).setCellValue("Ingresos");
                catHeader.createCell(2).setCellValue("Costos");
                catHeader.createCell(3).setCellValue("Margen");
                for (int i = 0; i < 4; i++) {
                    catHeader.getCell(i).setCellStyle(headerStyle);
                }

                for (ReporteRentabilidadResponse.RentabilidadCategoria cat : reporte.getCategorias()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(cat.getCategoriaNombre());
                    row.createCell(1).setCellValue(formatCurrency(cat.getIngresos()));
                    row.createCell(2).setCellValue(formatCurrency(cat.getCostos()));
                    row.createCell(3).setCellValue(formatCurrency(cat.getMargen()));
                }
            }

            // Autoajustar columnas
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ==============================
    // REPORTE DE ESTADÍSTICAS DE VENTAS
    // ==============================

    /**
     * Genera un archivo Excel con el reporte de estadísticas de ventas.
     *
     * @param estadisticas Estadísticas de ventas.
     * @return byte[] con el contenido del archivo Excel.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static byte[] generarExcelEstadisticas(EstadisticasVentasResponse estadisticas) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Hoja de resumen
            Sheet sheetResumen = workbook.createSheet("Resumen");
            int rowNum = 0;

            CellStyle headerStyle = crearEstiloHeader(workbook);
            CellStyle currencyStyle = crearEstiloMoneda(workbook);

            // Título
            Row titleRow = sheetResumen.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("ESTADÍSTICAS DE VENTAS");
            titleCell.setCellStyle(crearEstiloTitulo(workbook));
            sheetResumen.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

            // Período
            Row periodoRow = sheetResumen.createRow(rowNum++);
            periodoRow.createCell(0).setCellValue(
                    "Período: " + estadisticas.getFechaInicio().format(DATETIME_FORMATTER) +
                            " al " + estadisticas.getFechaFin().format(DATETIME_FORMATTER));
            sheetResumen.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 3));

            // Variación
            if (estadisticas.getVariacionPorcentual() != null) {
                Row varRow = sheetResumen.createRow(rowNum++);
                varRow.createCell(0).setCellValue(
                        "Variación vs período anterior: " +
                                estadisticas.getVariacionPorcentual().setScale(2, java.math.RoundingMode.HALF_UP) + "%");
                sheetResumen.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));
            }

            rowNum++; // Línea en blanco

            // Métricas principales
            Row metricRow = sheetResumen.createRow(rowNum++);
            metricRow.createCell(0).setCellValue("MÉTRICAS PRINCIPALES");
            metricRow.getCell(0).setCellStyle(crearEstiloSubTitulo(workbook));
            sheetResumen.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowNum - 1, rowNum - 1, 0, 3));

            String[][] metrics = {
                    {"Total Ventas", formatCurrency(estadisticas.getTotalVentas())},
                    {"Total Transacciones", String.valueOf(estadisticas.getTotalTransacciones())},
                    {"Ticket Promedio", formatCurrency(estadisticas.getTicketPromedio())}
            };

            for (String[] data : metrics) {
                Row row = sheetResumen.createRow(rowNum++);
                row.createCell(0).setCellValue(data[0]);
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(data[1].replace("S/", "").trim());
                if (data[0].equals("Total Ventas") || data[0].equals("Ticket Promedio")) {
                    valueCell.setCellStyle(currencyStyle);
                }
            }

            // Distribución por medio de pago
            if (estadisticas.getDistribucionMediosPago() != null && !estadisticas.getDistribucionMediosPago().isEmpty()) {
                rowNum++; // Línea en blanco

                // Crear hoja separada para distribución
                Sheet sheetDist = workbook.createSheet("Distribución por Pago");
                int distRow = 0;

                Row distTitle = sheetDist.createRow(distRow++);
                distTitle.createCell(0).setCellValue("DISTRIBUCIÓN POR MEDIO DE PAGO");
                distTitle.getCell(0).setCellStyle(crearEstiloTitulo(workbook));
                sheetDist.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 2));

                distRow++; // Línea en blanco

                Row distHeader = sheetDist.createRow(distRow++);
                distHeader.createCell(0).setCellValue("Medio de Pago");
                distHeader.createCell(1).setCellValue("Total");
                distHeader.createCell(2).setCellValue("Transacciones");
                for (int i = 0; i < 3; i++) {
                    distHeader.getCell(i).setCellStyle(headerStyle);
                }

                for (EstadisticasVentasResponse.DistribucionPago dp : estadisticas.getDistribucionMediosPago()) {
                    Row row = sheetDist.createRow(distRow++);
                    row.createCell(0).setCellValue(dp.getMedioPago().name());
                    row.createCell(1).setCellValue(formatCurrency(dp.getTotal()));
                    row.createCell(2).setCellValue(dp.getCantidadTransacciones());
                }

                for (int i = 0; i < 3; i++) {
                    sheetDist.autoSizeColumn(i);
                }
            }

            // Productos más vendidos
            if (estadisticas.getProductosMasVendidos() != null && !estadisticas.getProductosMasVendidos().isEmpty()) {
                // Crear hoja separada para top productos
                Sheet sheetTop = workbook.createSheet("Top Productos");
                int topRow = 0;

                Row topTitle = sheetTop.createRow(topRow++);
                topTitle.createCell(0).setCellValue("PRODUCTOS MÁS VENDIDOS");
                topTitle.getCell(0).setCellStyle(crearEstiloTitulo(workbook));
                sheetTop.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 3));

                topRow++; // Línea en blanco

                Row topHeader = sheetTop.createRow(topRow++);
                topHeader.createCell(0).setCellValue("Producto");
                topHeader.createCell(1).setCellValue("Cantidad Vendida");
                topHeader.createCell(2).setCellValue("Total Facturado");
                topHeader.createCell(3).setCellValue("% del Total");
                for (int i = 0; i < 4; i++) {
                    topHeader.getCell(i).setCellStyle(headerStyle);
                }

                BigDecimal totalVentas = estadisticas.getTotalVentas();
                for (EstadisticasVentasResponse.ProductoTop pt : estadisticas.getProductosMasVendidos()) {
                    Row row = sheetTop.createRow(topRow++);
                    row.createCell(0).setCellValue(pt.getProductoNombre());
                    row.createCell(1).setCellValue(pt.getCantidadVendida());
                    row.createCell(2).setCellValue(formatCurrency(pt.getTotalFacturado()));

                    // Calcular porcentaje
                    String porcentaje = "0%";
                    if (totalVentas != null && totalVentas.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal pct = pt.getTotalFacturado().divide(totalVentas, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        porcentaje = pct.setScale(1, java.math.RoundingMode.HALF_UP) + "%";
                    }
                    row.createCell(3).setCellValue(porcentaje);
                }

                for (int i = 0; i < 4; i++) {
                    sheetTop.autoSizeColumn(i);
                }
            }

            // Tendencia diaria
            if (estadisticas.getTendenciaDiaria() != null && !estadisticas.getTendenciaDiaria().isEmpty()) {
                Sheet sheetTend = workbook.createSheet("Tendencia Diaria");
                int tendRow = 0;

                Row tendTitle = sheetTend.createRow(tendRow++);
                tendTitle.createCell(0).setCellValue("TENDENCIA DIARIA DE VENTAS");
                tendTitle.getCell(0).setCellStyle(crearEstiloTitulo(workbook));
                sheetTend.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1));

                tendRow++; // Línea en blanco

                Row tendHeader = sheetTend.createRow(tendRow++);
                tendHeader.createCell(0).setCellValue("Fecha");
                tendHeader.createCell(1).setCellValue("Total");
                for (int i = 0; i < 2; i++) {
                    tendHeader.getCell(i).setCellStyle(headerStyle);
                }

                for (EstadisticasVentasResponse.TendenciaDiaria td : estadisticas.getTendenciaDiaria()) {
                    Row row = sheetTend.createRow(tendRow++);
                    row.createCell(0).setCellValue(td.getFecha().format(DATE_FORMATTER));
                    row.createCell(1).setCellValue(formatCurrency(td.getTotal()));
                }

                for (int i = 0; i < 2; i++) {
                    sheetTend.autoSizeColumn(i);
                }
            }

            // Autoajustar columnas del resumen
            for (int i = 0; i < 4; i++) {
                sheetResumen.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // ==============================
    // MÉTODOS AUXILIARES (ESTILOS)
    // ==============================

    private static CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloSubTitulo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloHeader(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private static CellStyle crearEstiloMoneda(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

   /* private static CellStyle crearEstiloNegativo(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.RED.getIndex());
        style.setFont(font);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }*/

    private static CellStyle crearEstiloFecha(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy"));
        return style;
    }

    private static CellStyle crearEstiloEntero(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));
        return style;
    }

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "S/ 0.00";
        return "S/ " + value.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
}