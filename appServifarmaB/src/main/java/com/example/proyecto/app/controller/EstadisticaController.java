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

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @Operation(summary = "Obtiene resumen del día actual", 
               description = "Retorna métricas clave del día actual (ventas, transacciones, ticket promedio)")
    public ResponseEntity<Map<String, Object>> obtenerResumenDiario() {
        log.debug("Solicitud de resumen diario");
        Map<String, Object> resumen = estadisticaService.obtenerResumenDiario();
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/resumen-diario/{fecha}")
    @Operation(summary = "Obtiene resumen de una fecha específica", 
               description = "Retorna métricas clave de una fecha determinada")
    public ResponseEntity<Map<String, Object>> obtenerResumenDiarioPorFecha(
            @Parameter(description = "Fecha en formato yyyy-MM-dd", example = "2025-06-20")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        log.debug("Solicitud de resumen diario para fecha: {}", fecha);
        Map<String, Object> resumen = estadisticaService.obtenerResumenDiario(fecha);
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/resumen-semanal")
    @Operation(summary = "Obtiene resumen de la semana actual", 
               description = "Retorna métricas clave de la semana en curso (lunes a domingo)")
    public ResponseEntity<Map<String, Object>> obtenerResumenSemanal() {
        log.debug("Solicitud de resumen semanal");
        Map<String, Object> resumen = estadisticaService.obtenerResumenSemanal();
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/resumen-mensual")
    @Operation(summary = "Obtiene resumen del mes actual", 
               description = "Retorna métricas clave del mes en curso")
    public ResponseEntity<Map<String, Object>> obtenerResumenMensual() {
        log.debug("Solicitud de resumen mensual");
        Map<String, Object> resumen = estadisticaService.obtenerResumenMensual();
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/resumen-mensual/{anio}/{mes}")
    @Operation(summary = "Obtiene resumen de un mes específico", 
               description = "Retorna métricas clave de un mes determinado")
    public ResponseEntity<Map<String, Object>> obtenerResumenMensualEspecifico(
            @Parameter(description = "Año (4 dígitos)", example = "2025")
            @PathVariable int anio,
            @Parameter(description = "Mes (1-12)", example = "6")
            @PathVariable int mes) {
        log.debug("Solicitud de resumen mensual para {}/{}", mes, anio);
        Map<String, Object> resumen = estadisticaService.obtenerResumenMensual(anio, mes);
        return ResponseEntity.ok(resumen);
    }

    // ==============================
    // DISTRIBUCIÓN POR MEDIOS DE PAGO
    // ==============================

    @GetMapping("/distribucion-pagos")
    @Operation(summary = "Obtiene distribución de ventas por medio de pago", 
               description = "Retorna distribución de ventas por medio de pago en un rango de fechas")
    public ResponseEntity<List<Object[]>> obtenerDistribucionMediosPago(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de distribución de medios de pago entre {} y {}", inicio, fin);
        List<Object[]> distribucion = estadisticaService.obtenerDistribucionMediosPago(inicio, fin);
        return ResponseEntity.ok(distribucion);
    }

    @GetMapping("/distribucion-pagos/diario")
    @Operation(summary = "Obtiene distribución de ventas por medio de pago del día actual", 
               description = "Retorna distribución de medios de pago para el día actual")
    public ResponseEntity<List<Object[]>> obtenerDistribucionMediosPagoDiario() {
        log.debug("Solicitud de distribución de medios de pago diario");
        List<Object[]> distribucion = estadisticaService.obtenerDistribucionMediosPagoDiario();
        return ResponseEntity.ok(distribucion);
    }

    // ==============================
    // TENDENCIAS POR HORA Y DÍA
    // ==============================

    @GetMapping("/ventas-por-hora/{fecha}")
    @Operation(summary = "Obtiene ventas agrupadas por hora para una fecha específica",
               description = "Retorna tendencia horaria de ventas para una fecha determinada")
    public ResponseEntity<List<Object[]>> obtenerVentasPorHora(
            @Parameter(description = "Fecha en formato yyyy-MM-dd", example = "2025-06-20")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        log.debug("Solicitud de ventas por hora para fecha: {}", fecha);
        List<Object[]> ventas = estadisticaService.obtenerVentasPorHora(fecha);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/ventas-por-dia")
    @Operation(summary = "Obtiene ventas agrupadas por día en un rango de fechas",
               description = "Retorna tendencia diaria de ventas entre dos fechas")
    public ResponseEntity<List<Object[]>> obtenerVentasPorDia(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd)", example = "2025-06-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd)", example = "2025-06-30")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {
        log.debug("Solicitud de ventas por día entre {} y {}", inicio, fin);
        List<Object[]> ventas = estadisticaService.obtenerVentasPorDia(inicio, fin);
        return ResponseEntity.ok(ventas);
    }

    // ==============================
    // INDICADORES CLAVE (KPI)
    // ==============================

    @GetMapping("/ticket-promedio")
    @Operation(summary = "Obtiene el ticket promedio en un rango de fechas",
               description = "Retorna el valor promedio de venta por transacción en el período indicado")
    public ResponseEntity<java.math.BigDecimal> obtenerTicketPromedio(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de ticket promedio entre {} y {}", inicio, fin);
        java.math.BigDecimal ticket = estadisticaService.obtenerTicketPromedio(inicio, fin);
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/total-ventas")
    @Operation(summary = "Obtiene el total de ventas en un rango de fechas",
               description = "Retorna la suma total de todas las ventas en el período indicado")
    public ResponseEntity<java.math.BigDecimal> obtenerTotalVentas(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de total de ventas entre {} y {}", inicio, fin);
        java.math.BigDecimal total = estadisticaService.obtenerTotalVentas(inicio, fin);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/total-transacciones")
    @Operation(summary = "Obtiene el número total de transacciones en un rango de fechas",
               description = "Retorna la cantidad de boletas/facturas emitidas en el período indicado")
    public ResponseEntity<Long> obtenerTotalTransacciones(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de total de transacciones entre {} y {}", inicio, fin);
        Long transacciones = estadisticaService.obtenerTotalTransacciones(inicio, fin);
        return ResponseEntity.ok(transacciones);
    }
}