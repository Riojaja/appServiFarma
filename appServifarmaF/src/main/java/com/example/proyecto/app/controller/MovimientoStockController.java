package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.response.MovimientoStockResponse;
import com.example.proyecto.app.entity.MovimientoStock;
import com.example.proyecto.app.service.MovimientoStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/movimientos-stock")
@RequiredArgsConstructor
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    // ==============================
    // CONSULTAS POR FILTROS
    // ==============================

    /**
     * Obtiene todos los movimientos de stock de un lote específico (historial completo).
     */
    @GetMapping("/lote/{loteId}")
    public ResponseEntity<List<MovimientoStockResponse>> listarPorLote(@PathVariable Integer loteId) {
        log.debug("Solicitud de movimientos de stock para lote ID: {}", loteId);
        List<MovimientoStockResponse> responses = movimientoStockService.listarPorLote(loteId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene todos los movimientos de stock realizados por un usuario específico.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<MovimientoStockResponse>> listarPorUsuario(@PathVariable Integer usuarioId) {
        log.debug("Solicitud de movimientos de stock para usuario ID: {}", usuarioId);
        List<MovimientoStockResponse> responses = movimientoStockService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene movimientos de stock filtrados por tipo (compra, venta, ajuste, merma).
     */
    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<MovimientoStockResponse>> listarPorTipo(
            @PathVariable MovimientoStock.TipoMovimiento tipo) {
        log.debug("Solicitud de movimientos de stock por tipo: {}", tipo);
        List<MovimientoStockResponse> responses = movimientoStockService.listarPorTipo(tipo);
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene movimientos de stock en un rango de fechas.
     */
    @GetMapping("/fechas")
    public ResponseEntity<List<MovimientoStockResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de movimientos de stock entre {} y {}", inicio, fin);
        List<MovimientoStockResponse> responses = movimientoStockService.listarPorFecha(inicio, fin);
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene todos los movimientos de stock registrados en el sistema (auditoría general).
     */
    @GetMapping
    public ResponseEntity<List<MovimientoStockResponse>> listarTodos() {
        log.debug("Solicitud de listado de todos los movimientos de stock");
        List<MovimientoStockResponse> responses = movimientoStockService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    /**
     * Obtiene el historial de movimientos de un lote específico, filtrado por tipo.
     */
    @GetMapping("/lote/{loteId}/tipo")
    public ResponseEntity<List<MovimientoStockResponse>> listarPorLoteYTipo(
            @PathVariable Integer loteId,
            @RequestParam MovimientoStock.TipoMovimiento tipo) {
        log.debug("Solicitud de movimientos de stock para lote ID: {} y tipo: {}", loteId, tipo);
        List<MovimientoStockResponse> responses = movimientoStockService.listarPorLoteYTipo(loteId, tipo);
        return ResponseEntity.ok(responses);
    }
}