package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;
import com.example.proyecto.app.service.ReporteService;
import com.example.proyecto.app.util.ReporteCsvGenerator;
import com.example.proyecto.app.util.ReporteExcelGenerator;
import com.example.proyecto.app.util.ReportePdfGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para generación de reportes y estadísticas")
@PreAuthorize("hasRole('ADMIN')") // Solo administradores pueden acceder a reportes
public class ReporteController {

    private final ReporteService reporteService;

    // ==============================
    // REPORTE DIGEMIT
    // ==============================

    @GetMapping("/digemit")
    @Operation(summary = "Genera reporte mensual DIGEMIT", 
               description = "Exporta reporte de productos, lotes y vencimientos en formato Excel, PDF o CSV")
    public ResponseEntity<byte[]> generarReporteDigemit(
            @Parameter(description = "Mes del reporte (formato: YYYY-MM)", example = "2025-06")
            @RequestParam String mes,
            
            @Parameter(description = "Formato de salida: excel, pdf o csv", example = "excel")
            @RequestParam(defaultValue = "excel") String formato) {

        log.info("Solicitud de reporte DIGEMIT para mes: {}, formato: {}", mes, formato);

        ReporteDigemitResponse reporte = reporteService.generarReporteDigemit(mes);

        try {
            byte[] bytes;
            String filename;
            MediaType mediaType;

            switch (formato.toLowerCase()) {
                case "pdf":
                    bytes = ReportePdfGenerator.generarPdfDigemit(reporte);
                    filename = "reporte_digemit_" + mes + ".pdf";
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                case "csv":
                    bytes = ReporteCsvGenerator.generarCsvDigemit(reporte);
                    filename = "reporte_digemit_" + mes + ".csv";
                    mediaType = new MediaType("text", "csv");
                    break;
                case "excel":
                default:
                    bytes = ReporteExcelGenerator.generarExcelDigemit(reporte);
                    filename = "reporte_digemit_" + mes + ".xlsx";
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
            }

            return buildFileResponse(bytes, filename, mediaType);

        } catch (Exception e) {
            log.error("Error al generar reporte DIGEMIT: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    // ==============================
    // REPORTE DE RENTABILIDAD
    // ==============================
    
    @GetMapping("/rentabilidad/json")
    @Operation(summary = "Obtiene datos de rentabilidad en JSON",
               description = "Retorna los datos de rentabilidad en formato JSON para el frontend")
    public ResponseEntity<ReporteRentabilidadResponse> obtenerRentabilidadJson(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        log.info("Solicitud de datos de rentabilidad JSON desde {} hasta {}", inicio, fin);
        ReporteRentabilidadResponse reporte = reporteService.generarReporteRentabilidad(inicio, fin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/rentabilidad")
    @Operation(summary = "Genera reporte de rentabilidad real",
               description = "Exporta reporte de rentabilidad descontando mermas y costos de compra")
    public ResponseEntity<byte[]> generarReporteRentabilidad(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            
            @Parameter(description = "Formato de salida: excel, pdf o csv", example = "excel")
            @RequestParam(defaultValue = "excel") String formato) {

        log.info("Solicitud de reporte de rentabilidad desde {} hasta {}, formato: {}", inicio, fin, formato);

        ReporteRentabilidadResponse reporte = reporteService.generarReporteRentabilidad(inicio, fin);

        try {
            byte[] bytes;
            String filename = "rentabilidad_" + inicio + "_" + fin;
            MediaType mediaType;

            switch (formato.toLowerCase()) {
                case "pdf":
                    bytes = ReportePdfGenerator.generarPdfRentabilidad(reporte);
                    filename += ".pdf";
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                case "csv":
                    bytes = ReporteCsvGenerator.generarCsvRentabilidad(reporte);
                    filename += ".csv";
                    mediaType = new MediaType("text", "csv");
                    break;
                case "excel":
                default:
                    bytes = ReporteExcelGenerator.generarExcelRentabilidad(reporte);
                    filename += ".xlsx";
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
            }

            return buildFileResponse(bytes, filename, mediaType);

        } catch (Exception e) {
            log.error("Error al generar reporte de rentabilidad: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    // ==============================
    // ESTADÍSTICAS DE VENTAS (RANGO DE FECHAS)
    // ==============================

    @GetMapping("/estadisticas")
    @Operation(summary = "Genera estadísticas de ventas por período",
               description = "Exporta estadísticas de ventas con distribución por pago, top productos y tendencia")
    public ResponseEntity<byte[]> generarEstadisticasVentas(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            
            @Parameter(description = "Formato de salida: excel, pdf o csv", example = "excel")
            @RequestParam(defaultValue = "excel") String formato) {

        log.info("Solicitud de estadísticas de ventas desde {} hasta {}, formato: {}", inicio, fin, formato);

        EstadisticasVentasResponse estadisticas = reporteService.generarEstadisticasVentas(inicio, fin);

        try {
            byte[] bytes;
            String filename = "estadisticas_ventas_" + inicio.format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                              "_" + fin.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            MediaType mediaType;

            switch (formato.toLowerCase()) {
                case "pdf":
                    bytes = ReportePdfGenerator.generarPdfEstadisticas(estadisticas);
                    filename += ".pdf";
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                case "csv":
                    bytes = ReporteCsvGenerator.generarCsvEstadisticas(estadisticas);
                    filename += ".csv";
                    mediaType = new MediaType("text", "csv");
                    break;
                case "excel":
                default:
                    bytes = ReporteExcelGenerator.generarExcelEstadisticas(estadisticas);
                    filename += ".xlsx";
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
            }

            return buildFileResponse(bytes, filename, mediaType);

        } catch (Exception e) {
            log.error("Error al generar estadísticas de ventas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    // ==============================
    // ESTADÍSTICAS DE VENTAS POR PERÍODO (DIA/SEMANA/MES)
    // ==============================

    @GetMapping("/estadisticas/periodo")
    @Operation(summary = "Genera estadísticas de ventas por período (día, semana, mes)",
               description = "Exporta estadísticas basadas en un período relativo")
    public ResponseEntity<byte[]> generarEstadisticasPorPeriodo(
            @Parameter(description = "Tipo de período: dia, semana o mes", example = "mes")
            @RequestParam String periodo,
            
            @Parameter(description = "Fecha de referencia (formato: yyyy-MM-dd)", example = "2025-06-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaReferencia,
            
            @Parameter(description = "Formato de salida: excel, pdf o csv", example = "excel")
            @RequestParam(defaultValue = "excel") String formato) {

        log.info("Solicitud de estadísticas de ventas por período: {}, fecha: {}, formato: {}",
                periodo, fechaReferencia, formato);

        EstadisticasVentasResponse estadisticas = reporteService.generarEstadisticasPorPeriodo(periodo, fechaReferencia);

        try {
            byte[] bytes;
            String filename = "estadisticas_" + periodo + "_" + fechaReferencia;
            MediaType mediaType;

            switch (formato.toLowerCase()) {
                case "pdf":
                    bytes = ReportePdfGenerator.generarPdfEstadisticas(estadisticas);
                    filename += ".pdf";
                    mediaType = MediaType.APPLICATION_PDF;
                    break;
                case "csv":
                    bytes = ReporteCsvGenerator.generarCsvEstadisticas(estadisticas);
                    filename += ".csv";
                    mediaType = new MediaType("text", "csv");
                    break;
                case "excel":
                default:
                    bytes = ReporteExcelGenerator.generarExcelEstadisticas(estadisticas);
                    filename += ".xlsx";
                    mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    break;
            }

            return buildFileResponse(bytes, filename, mediaType);

        } catch (Exception e) {
            log.error("Error al generar estadísticas por período: {}", e.getMessage(), e);
            throw new RuntimeException("Error al generar el reporte: " + e.getMessage(), e);
        }
    }

    // ==============================
    // MÉTODO AUXILIAR PARA CONSTRUIR RESPUESTA
    // ==============================

    private ResponseEntity<byte[]> buildFileResponse(byte[] bytes, String filename, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(bytes.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }
}