package com.example.proyecto.app.controller;

import com.example.proyecto.app.entity.MovimientoStock;
import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
@Tag(name = "Auditoría", description = "Endpoints para consultas de auditoría y trazabilidad")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    // ==============================
    // AUDITORÍA DE MOVIMIENTOS DE STOCK
    // ==============================

    @GetMapping("/movimientos/usuario/{usuarioId}")
    @Operation(summary = "Obtiene movimientos de stock por usuario",
               description = "Retorna todos los movimientos de stock realizados por un usuario en un rango de fechas")
    public ResponseEntity<List<MovimientoStock>> obtenerMovimientosPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId,
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de movimientos de stock para usuario ID: {} entre {} y {}", usuarioId, inicio, fin);
        List<MovimientoStock> movimientos = auditoriaService.obtenerMovimientosPorUsuario(usuarioId, inicio, fin);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/movimientos/tipo/{tipo}")
    @Operation(summary = "Obtiene movimientos de stock por tipo",
               description = "Retorna movimientos de stock filtrados por tipo (compra, venta, ajuste, merma) en un rango de fechas")
    public ResponseEntity<List<MovimientoStock>> obtenerMovimientosPorTipo(
            @Parameter(description = "Tipo de movimiento: compra, venta, ajuste, merma", example = "venta")
            @PathVariable MovimientoStock.TipoMovimiento tipo,
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de movimientos de stock por tipo: {} entre {} y {}", tipo, inicio, fin);
        List<MovimientoStock> movimientos = auditoriaService.obtenerMovimientosPorTipo(tipo, inicio, fin);
        return ResponseEntity.ok(movimientos);
    }

    @GetMapping("/movimientos/lote/{loteId}")
    @Operation(summary = "Obtiene historial completo de movimientos de un lote",
               description = "Retorna todos los movimientos de stock de un lote específico (ordenados por fecha descendente)")
    public ResponseEntity<List<MovimientoStock>> obtenerHistorialLote(
            @Parameter(description = "ID del lote", example = "1")
            @PathVariable Integer loteId) {
        log.debug("Solicitud de historial completo del lote ID: {}", loteId);
        List<MovimientoStock> historial = auditoriaService.obtenerHistorialLote(loteId);
        return ResponseEntity.ok(historial);
    }

    // ==============================
    // AUDITORÍA DE VENTAS
    // ==============================

    @GetMapping("/ventas/anuladas")
    @Operation(summary = "Obtiene ventas anuladas",
               description = "Retorna todas las ventas anuladas en un rango de fechas")
    public ResponseEntity<List<Venta>> obtenerVentasAnuladas(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de ventas anuladas entre {} y {}", inicio, fin);
        List<Venta> ventas = auditoriaService.obtenerVentasAnuladas(inicio, fin);
        return ResponseEntity.ok(ventas);
    }

    @GetMapping("/ventas/usuario/{usuarioId}")
    @Operation(summary = "Obtiene ventas por usuario",
               description = "Retorna todas las ventas realizadas por un usuario en un rango de fechas")
    public ResponseEntity<List<Venta>> obtenerVentasPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId,
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de ventas del usuario ID: {} entre {} y {}", usuarioId, inicio, fin);
        List<Venta> ventas = auditoriaService.obtenerVentasPorUsuario(usuarioId, inicio, fin);
        return ResponseEntity.ok(ventas);
    }

    // ==============================
    // AUDITORÍA DE CAJA
    // ==============================

    @GetMapping("/caja/usuario/{usuarioId}")
    @Operation(summary = "Obtiene historial de caja por usuario",
               description = "Retorna todas las aperturas y cierres de caja realizados por un usuario")
    public ResponseEntity<List<Map<String, Object>>> obtenerHistorialCajaPorUsuario(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer usuarioId) {
        log.debug("Solicitud de historial de caja para usuario ID: {}", usuarioId);
        List<Map<String, Object>> historial = auditoriaService.obtenerHistorialCajaPorUsuario(usuarioId);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/caja/diferencias")
    @Operation(summary = "Obtiene cajas con diferencia (sobrante/faltante)",
               description = "Retorna cajas cerradas que tuvieron diferencia entre monto declarado y ventas totales")
    public ResponseEntity<List<Map<String, Object>>> obtenerCajasConDiferencia(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de cajas con diferencia entre {} y {}", inicio, fin);
        List<Map<String, Object>> diferencias = auditoriaService.obtenerCajasConDiferencia(inicio, fin);
        return ResponseEntity.ok(diferencias);
    }

    // ==============================
    // AUDITORÍA GENERAL
    // ==============================

    @GetMapping("/resumen-actividad")
    @Operation(summary = "Obtiene resumen de actividad del sistema",
               description = "Retorna métricas consolidadas de actividad en un rango de fechas")
    public ResponseEntity<Map<String, Object>> obtenerResumenActividad(
            @Parameter(description = "Fecha y hora de inicio (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha y hora de fin (formato: yyyy-MM-ddTHH:mm:ss)", example = "2025-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de resumen de actividad entre {} y {}", inicio, fin);
        Map<String, Object> resumen = auditoriaService.obtenerResumenActividad(inicio, fin);
        return ResponseEntity.ok(resumen);
    }
}