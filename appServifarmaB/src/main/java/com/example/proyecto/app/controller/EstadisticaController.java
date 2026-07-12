package com.example.proyecto.app.controller;

import com.example.proyecto.app.service.EstadisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/estadisticas")
@RequiredArgsConstructor
@Tag(name = "Estadísticas", description = "Endpoints para estadísticas y métricas del dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class EstadisticaController {

    private final EstadisticaService estadisticaService;

    // ==============================
    // RESÚMENES PARA DASHBOARD
    // ==============================

    @GetMapping("/resumen-diario")
    @Operation(summary = "Obtiene resumen del día actual")
    public ResponseEntity<Map<String, Object>> obtenerResumenDiario() {
        log.debug("Solicitud de resumen diario");
        return ResponseEntity.ok(estadisticaService.obtenerResumenDiario());
    }

    @GetMapping("/resumen-diario/{fecha}")
    @Operation(summary = "Obtiene resumen de una fecha específica")
    public ResponseEntity<Map<String, Object>> obtenerResumenDiarioPorFecha(
            @Parameter(description = "Fecha en formato yyyy-MM-dd", example = "2025-06-20")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        log.debug("Solicitud de resumen diario para fecha: {}", fecha);
        return ResponseEntity.ok(estadisticaService.obtenerResumenDiario(fecha));
    }

    @GetMapping("/resumen-semanal")
    @Operation(summary = "Obtiene resumen de la semana actual")
    public ResponseEntity<Map<String, Object>> obtenerResumenSemanal() {
        log.debug("Solicitud de resumen semanal");
        return ResponseEntity.ok(estadisticaService.obtenerResumenSemanal());
    }

    @GetMapping("/resumen-mensual")
    @Operation(summary = "Obtiene resumen del mes actual")
    public ResponseEntity<Map<String, Object>> obtenerResumenMensual() {
        log.debug("Solicitud de resumen mensual");
        return ResponseEntity.ok(estadisticaService.obtenerResumenMensual());
    }

    @GetMapping("/resumen-mensual/{anio}/{mes}")
    @Operation(summary = "Obtiene resumen de un mes específico")
    public ResponseEntity<Map<String, Object>> obtenerResumenMensualEspecifico(
            @Parameter(description = "Año (4 dígitos)", example = "2025")
            @PathVariable int anio,
            @Parameter(description = "Mes (1-12)", example = "6")
            @PathVariable int mes) {
        log.debug("Solicitud de resumen mensual para {}/{}", mes, anio);
        return ResponseEntity.ok(estadisticaService.obtenerResumenMensual(anio, mes));
    }

    // ==============================
    // DISTRIBUCIÓN POR MEDIOS DE PAGO
    // ==============================

    @GetMapping("/distribucion-pagos")
    @Operation(summary = "Obtiene distribución de ventas por medio de pago en un rango de fechas")
    public ResponseEntity<List<Object[]>> obtenerDistribucionMediosPago(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        log.debug("Solicitud de distribución de medios de pago entre {} y {}", inicioDateTime, finDateTime);
        
        List<Object[]> distribucion = estadisticaService.obtenerDistribucionMediosPago(inicioDateTime, finDateTime);
        return ResponseEntity.ok(distribucion);
    }

    @GetMapping("/distribucion-pagos/diario")
    @Operation(summary = "Obtiene distribución de ventas por medio de pago del día actual")
    public ResponseEntity<List<Object[]>> obtenerDistribucionMediosPagoDiario() {
        log.debug("Solicitud de distribución de medios de pago diario");
        return ResponseEntity.ok(estadisticaService.obtenerDistribucionMediosPagoDiario());
    }

    // ==============================
    // TENDENCIAS POR HORA Y DÍA
    // ==============================

    @GetMapping("/ventas-por-hora/{fecha}")
    @Operation(summary = "Obtiene ventas agrupadas por hora para una fecha específica")
    public ResponseEntity<List<Object[]>> obtenerVentasPorHora(
            @Parameter(description = "Fecha en formato yyyy-MM-dd", example = "2025-06-20")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        log.debug("Solicitud de ventas por hora para fecha: {}", fecha);
        List<Object[]> ventas = estadisticaService.obtenerVentasPorHora(fecha);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/ventas-por-dia")
    @Operation(summary = "Obtiene ventas agrupadas por día en un rango de fechas")
    public ResponseEntity<List<Object[]>> obtenerVentasPorDia(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        // ✅ CORRECCIÓN: llamamos directamente al método que acepta LocalDate
        log.debug("Solicitud de ventas por día entre {} y {}", inicio, fin);
        List<Object[]> ventas = estadisticaService.obtenerVentasPorDia(inicio, fin);
        return ResponseEntity.ok(ventas);
    }

    // ==============================
    // INDICADORES CLAVE (KPI)
    // ==============================

    @GetMapping("/ticket-promedio")
    @Operation(summary = "Obtiene el ticket promedio en un rango de fechas")
    public ResponseEntity<BigDecimal> obtenerTicketPromedio(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        log.debug("Solicitud de ticket promedio entre {} y {}", inicioDateTime, finDateTime);
        
        BigDecimal ticket = estadisticaService.obtenerTicketPromedio(inicioDateTime, finDateTime);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/total-ventas")
    @Operation(summary = "Obtiene el total de ventas en un rango de fechas")
    public ResponseEntity<BigDecimal> obtenerTotalVentas(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        log.debug("Solicitud de total de ventas entre {} y {}", inicioDateTime, finDateTime);
        
        BigDecimal total = estadisticaService.obtenerTotalVentas(inicioDateTime, finDateTime);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total-transacciones")
    @Operation(summary = "Obtiene el número total de transacciones en un rango de fechas")
    public ResponseEntity<Long> obtenerTotalTransacciones(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        
        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);
        log.debug("Solicitud de total de transacciones entre {} y {}", inicioDateTime, finDateTime);
        
        Long transacciones = estadisticaService.obtenerTotalTransacciones(inicioDateTime, finDateTime);
        return ResponseEntity.ok(transacciones);
    }
}