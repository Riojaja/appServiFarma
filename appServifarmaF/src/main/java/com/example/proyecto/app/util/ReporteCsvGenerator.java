package com.example.proyecto.app.util;

import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para generar reportes en formato CSV.
 * Proporciona métodos para exportar reportes DIGEMIT, rentabilidad y estadísticas de ventas.
 */
public final class ReporteCsvGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String DELIMITER = ",";
    //private static final String LINE_SEPARATOR = "\n";
    private static final String QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";

    private ReporteCsvGenerator() {
        // Constructor privado para evitar instanciación
    }

    // ==============================
    // REPORTE DIGEMIT
    // ==============================

    /**
     * Genera un archivo CSV con el reporte DIGEMIT.
     *
     * @param reporte Reporte DIGEMIT generado.
     * @return byte[] con el contenido del archivo CSV.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static byte[] generarCsvDigemit(ReporteDigemitResponse reporte) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // Escribir encabezado
            writer.println("REPORTE DIGEMIT - " + reporte.getMes());
            writer.println("Fecha de generación: " + reporte.getFechaGeneracion().format(DATE_FORMATTER));
            writer.println();

            // Encabezados de columnas
            writer.println(String.join(DELIMITER,
                    "Código",
                    "Producto",
                    "Laboratorio",
                    "Principio Activo",
                    "Lote",
                    "Vencimiento",
                    "Cantidad"
            ));

            // Datos
            for (ReporteDigemitResponse.Item item : reporte.getItems()) {
                writer.println(String.join(DELIMITER,
                        escapeCsv(item.getCodigoProducto()),
                        escapeCsv(item.getNombreProducto()),
                        escapeCsv(item.getLaboratorio()),
                        escapeCsv(item.getPrincipioActivo()),
                        escapeCsv(item.getLote()),
                        escapeCsv(item.getFechaVencimiento().format(DATE_FORMATTER)),
                        String.valueOf(item.getCantidad())
                ));
            }

            writer.flush();
            return baos.toByteArray();
        }
    }

    // ==============================
    // REPORTE DE RENTABILIDAD
    // ==============================

    /**
     * Genera un archivo CSV con el reporte de rentabilidad.
     *
     * @param reporte Reporte de rentabilidad generado.
     * @return byte[] con el contenido del archivo CSV.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static byte[] generarCsvRentabilidad(ReporteRentabilidadResponse reporte) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // Encabezado
            writer.println("REPORTE DE RENTABILIDAD REAL");
            writer.println("Período: " + reporte.getFechaInicio().format(DATE_FORMATTER) +
                    " al " + reporte.getFechaFin().format(DATE_FORMATTER));
            writer.println();

            // Resumen general
            writer.println("RESUMEN GENERAL");
            writer.println(String.join(DELIMITER, "Métrica", "Valor"));
            writer.println(String.join(DELIMITER, "Ingresos Totales", formatCurrency(reporte.getIngresosTotales())));
            writer.println(String.join(DELIMITER, "Costo de Ventas", formatCurrency(reporte.getCostoVentas())));
            writer.println(String.join(DELIMITER, "Mermas (pérdidas)", formatCurrency(reporte.getMermas())));
            writer.println(String.join(DELIMITER, "Margen Bruto", formatCurrency(reporte.getMargenBruto())));
            writer.println(String.join(DELIMITER, "Margen Neto (real)", formatCurrency(reporte.getMargenNeto())));
            writer.println();

            // Desglose por categoría
            if (reporte.getCategorias() != null && !reporte.getCategorias().isEmpty()) {
                writer.println("DESGLOSE POR CATEGORÍA");
                writer.println(String.join(DELIMITER, "Categoría", "Ingresos", "Costos", "Margen"));
                for (ReporteRentabilidadResponse.RentabilidadCategoria cat : reporte.getCategorias()) {
                    writer.println(String.join(DELIMITER,
                            escapeCsv(cat.getCategoriaNombre()),
                            formatCurrency(cat.getIngresos()),
                            formatCurrency(cat.getCostos()),
                            formatCurrency(cat.getMargen())
                    ));
                }
            }

            writer.flush();
            return baos.toByteArray();
        }
    }

    // ==============================
    // REPORTE DE ESTADÍSTICAS DE VENTAS
    // ==============================

    /**
     * Genera un archivo CSV con el reporte de estadísticas de ventas.
     *
     * @param estadisticas Estadísticas de ventas.
     * @return byte[] con el contenido del archivo CSV.
     * @throws IOException Si ocurre un error al escribir el archivo.
     */
    public static byte[] generarCsvEstadisticas(EstadisticasVentasResponse estadisticas) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            // Título y período
            writer.println("ESTADÍSTICAS DE VENTAS");
            writer.println("Período: " + estadisticas.getFechaInicio().format(DATETIME_FORMATTER) +
                    " al " + estadisticas.getFechaFin().format(DATETIME_FORMATTER));
            if (estadisticas.getVariacionPorcentual() != null) {
                writer.println("Variación vs período anterior: " +
                        estadisticas.getVariacionPorcentual().setScale(2, java.math.RoundingMode.HALF_UP) + "%");
            }
            writer.println();

            // Métricas principales
            writer.println("MÉTRICAS PRINCIPALES");
            writer.println(String.join(DELIMITER, "Métrica", "Valor"));
            writer.println(String.join(DELIMITER, "Total Ventas", formatCurrency(estadisticas.getTotalVentas())));
            writer.println(String.join(DELIMITER, "Total Transacciones", String.valueOf(estadisticas.getTotalTransacciones())));
            writer.println(String.join(DELIMITER, "Ticket Promedio", formatCurrency(estadisticas.getTicketPromedio())));
            writer.println();

            // Distribución por medio de pago
            if (estadisticas.getDistribucionMediosPago() != null && !estadisticas.getDistribucionMediosPago().isEmpty()) {
                writer.println("DISTRIBUCIÓN POR MEDIO DE PAGO");
                writer.println(String.join(DELIMITER, "Medio de Pago", "Total", "Transacciones"));
                for (EstadisticasVentasResponse.DistribucionPago dp : estadisticas.getDistribucionMediosPago()) {
                    writer.println(String.join(DELIMITER,
                            dp.getMedioPago().name(),
                            formatCurrency(dp.getTotal()),
                            String.valueOf(dp.getCantidadTransacciones())
                    ));
                }
                writer.println();
            }

            // Productos más vendidos
            if (estadisticas.getProductosMasVendidos() != null && !estadisticas.getProductosMasVendidos().isEmpty()) {
                writer.println("PRODUCTOS MÁS VENDIDOS");
                writer.println(String.join(DELIMITER, "Producto", "Cantidad Vendida", "Total Facturado", "% del Total"));
                BigDecimal totalVentas = estadisticas.getTotalVentas();
                for (EstadisticasVentasResponse.ProductoTop pt : estadisticas.getProductosMasVendidos()) {
                    String porcentaje = "0%";
                    if (totalVentas != null && totalVentas.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal pct = pt.getTotalFacturado().divide(totalVentas, 4, java.math.RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100));
                        porcentaje = pct.setScale(1, java.math.RoundingMode.HALF_UP) + "%";
                    }
                    writer.println(String.join(DELIMITER,
                            escapeCsv(pt.getProductoNombre()),
                            String.valueOf(pt.getCantidadVendida()),
                            formatCurrency(pt.getTotalFacturado()),
                            porcentaje
                    ));
                }
                writer.println();
            }

            // Tendencia diaria
            if (estadisticas.getTendenciaDiaria() != null && !estadisticas.getTendenciaDiaria().isEmpty()) {
                writer.println("TENDENCIA DIARIA");
                writer.println(String.join(DELIMITER, "Fecha", "Total"));
                for (EstadisticasVentasResponse.TendenciaDiaria td : estadisticas.getTendenciaDiaria()) {
                    writer.println(String.join(DELIMITER,
                            td.getFecha().format(DATE_FORMATTER),
                            formatCurrency(td.getTotal())
                    ));
                }
            }

            writer.flush();
            return baos.toByteArray();
        }
    }

    // ==============================
    // MÉTODOS AUXILIARES
    // ==============================

    /**
     * Escapa un campo para CSV (envuelve entre comillas y escapa comillas internas).
     *
     * @param value Valor a escapar.
     * @return Valor escapado, o cadena vacía si es null.
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(DELIMITER) || value.contains(QUOTE) || value.contains("\n")) {
            return QUOTE + value.replace(QUOTE, ESCAPED_QUOTE) + QUOTE;
        }
        return value;
    }

    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "S/ 0.00";
        return "S/ " + value.setScale(2, java.math.RoundingMode.HALF_UP).toString();
    }
}