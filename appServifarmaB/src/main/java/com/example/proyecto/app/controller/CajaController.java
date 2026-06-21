package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.AperturaCajaRequest;
import com.example.proyecto.app.dto.request.CierreCajaRequest;
import com.example.proyecto.app.dto.response.CajaResponse;
import com.example.proyecto.app.dto.response.CierreCajaResponse;
import com.example.proyecto.app.entity.Caja;
import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.service.CajaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    // ==============================
    // OPERACIONES DE APERTURA Y CIERRE
    // ==============================

    @PostMapping("/apertura")
    public ResponseEntity<CajaResponse> abrirCaja(@Valid @RequestBody AperturaCajaRequest request) {
        log.debug("Solicitud de apertura de caja para usuario ID: {}", request.getUsuarioAperturaId());
        CajaResponse response = cajaService.abrirCaja(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/cierre")
    public ResponseEntity<CierreCajaResponse> cerrarCaja(@Valid @RequestBody CierreCajaRequest request) {
        log.debug("Solicitud de cierre de caja para usuario ID: {}", request.getUsuarioCierreId());
        CierreCajaResponse response = cajaService.cerrarCaja(request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // CONSULTAS DE CAJA
    // ==============================

    @GetMapping("/abierta")
    public ResponseEntity<CajaResponse> obtenerCajaAbierta() {
        log.debug("Solicitud de caja abierta actual");
        CajaResponse response = cajaService.obtenerCajaAbierta();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaResponse> obtenerCajaPorId(@PathVariable Integer id) {
        log.debug("Solicitud de caja con ID: {}", id);
        CajaResponse response = cajaService.obtenerCajaPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CajaResponse>> listarTodas() {
        log.debug("Solicitud de listado de todas las cajas");
        List<CajaResponse> responses = cajaService.listarTodas();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<CajaResponse>> listarPorUsuarioApertura(@PathVariable Integer usuarioId) {
        log.debug("Solicitud de cajas abiertas por usuario ID: {}", usuarioId);
        List<CajaResponse> responses = cajaService.listarPorUsuarioApertura(usuarioId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<CajaResponse>> listarPorFechasCierre(
            @RequestParam LocalDateTime inicio,
            @RequestParam LocalDateTime fin) {
        log.debug("Solicitud de cajas cerradas entre {} y {}", inicio, fin);
        List<CajaResponse> responses = cajaService.listarPorFechasCierre(inicio, fin);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<CajaResponse>> listarPorEstado(@PathVariable Caja.EstadoCaja estado) {
        log.debug("Solicitud de cajas por estado: {}", estado);
        List<CajaResponse> responses = cajaService.listarPorEstado(estado);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // VALIDACIONES Y CONSULTAS DE ESTADO
    // ==============================

    @GetMapping("/existe-abierta")
    public ResponseEntity<Boolean> existeCajaAbierta() {
        log.debug("Solicitud de verificación de caja abierta");
        boolean existe = cajaService.existeCajaAbierta();
        return ResponseEntity.ok(existe);
    }

    @GetMapping("/usuario/{usuarioId}/tiene-abierta")
    public ResponseEntity<Boolean> usuarioTieneCajaAbierta(@PathVariable Integer usuarioId) {
        log.debug("Solicitud de verificación de caja abierta para usuario ID: {}", usuarioId);
        boolean tiene = cajaService.usuarioTieneCajaAbierta(usuarioId);
        return ResponseEntity.ok(tiene);
    }

    @GetMapping("/{cajaId}/total-ventas")
    public ResponseEntity<BigDecimal> obtenerTotalVentasCaja(@PathVariable Integer cajaId) {
        log.debug("Solicitud de total de ventas para caja ID: {}", cajaId);
        BigDecimal total = cajaService.obtenerTotalVentasCaja(cajaId);
        return ResponseEntity.ok(total);
    }

    @GetMapping("/{cajaId}/total-ventas/{medioPago}")
    public ResponseEntity<BigDecimal> obtenerTotalVentasPorMedioPago(
            @PathVariable Integer cajaId,
            @PathVariable Venta.MedioPago medioPago) {
        log.debug("Solicitud de total de ventas para caja ID: {} y medio de pago: {}", cajaId, medioPago);
        BigDecimal total = cajaService.obtenerTotalVentasPorMedioPago(cajaId, medioPago);
        return ResponseEntity.ok(total);
    }
}
