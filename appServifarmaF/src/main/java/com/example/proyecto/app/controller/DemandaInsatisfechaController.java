package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.DemandaInsatisfechaRequest;
import com.example.proyecto.app.dto.response.DemandaInsatisfechaResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.service.DemandaInsatisfechaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/demandas-insatisfechas")
@RequiredArgsConstructor
public class DemandaInsatisfechaController {

    private final DemandaInsatisfechaService demandaService;

    // ==============================
    // OPERACIONES DE REGISTRO
    // ==============================

    @PostMapping
    public ResponseEntity<DemandaInsatisfechaResponse> crearDemanda(
            @Valid @RequestBody DemandaInsatisfechaRequest request) {
        log.debug("Solicitud de registro de demanda insatisfecha: {}", request.getProductoSolicitado());
        DemandaInsatisfechaResponse response = demandaService.crearDemanda(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==============================
    // CONSULTAS
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<DemandaInsatisfechaResponse> obtenerPorId(@PathVariable Integer id) {
        log.debug("Solicitud de demanda insatisfecha con ID: {}", id);
        DemandaInsatisfechaResponse response = demandaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DemandaInsatisfechaResponse>> listarTodas() {
        log.debug("Solicitud de listado de todas las demandas insatisfechas");
        List<DemandaInsatisfechaResponse> responses = demandaService.listarTodas();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<DemandaInsatisfechaResponse>> listarPorUsuario(@PathVariable Integer usuarioId) {
        log.debug("Solicitud de demandas insatisfechas del usuario ID: {}", usuarioId);
        List<DemandaInsatisfechaResponse> responses = demandaService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/producto")
    public ResponseEntity<List<DemandaInsatisfechaResponse>> listarPorProducto(
            @RequestParam String productoSolicitado) {
        log.debug("Solicitud de demandas insatisfechas por producto: {}", productoSolicitado);
        List<DemandaInsatisfechaResponse> responses = demandaService.listarPorProducto(productoSolicitado);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<DemandaInsatisfechaResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de demandas insatisfechas entre {} y {}", inicio, fin);
        List<DemandaInsatisfechaResponse> responses = demandaService.listarPorFecha(inicio, fin);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/cliente-documento")
    public ResponseEntity<List<DemandaInsatisfechaResponse>> listarPorClienteDocumento(
            @RequestParam String clienteDocumento) {
        log.debug("Solicitud de demandas insatisfechas para cliente con documento: {}", clienteDocumento);
        List<DemandaInsatisfechaResponse> responses = demandaService.listarPorClienteDocumento(clienteDocumento);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // ESTADÍSTICAS Y ELIMINACIÓN
    // ==============================

    @GetMapping("/contar-periodo")
    public ResponseEntity<Long> contarDemandasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de conteo de demandas insatisfechas entre {} y {}", inicio, fin);
        long count = demandaService.contarDemandasPorPeriodo(inicio, fin);
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarDemanda(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de demanda insatisfecha con ID: {}", id);
        demandaService.eliminarDemanda(id);
        return ResponseEntity.ok(new MensajeResponse("Demanda insatisfecha eliminada correctamente"));
    }
}