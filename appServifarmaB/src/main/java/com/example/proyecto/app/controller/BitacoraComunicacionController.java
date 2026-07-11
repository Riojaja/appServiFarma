package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.BitacoraComunicacionRequest;
import com.example.proyecto.app.dto.response.BitacoraComunicacionResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bitacora-comunicacion")
@RequiredArgsConstructor
public class BitacoraComunicacionController {

    private static final Logger log = LoggerFactory.getLogger(BitacoraComunicacionController.class);

    private final BitacoraComunicacionService bitacoraService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<BitacoraComunicacionResponse> crearMensaje(
            @Valid @RequestBody BitacoraComunicacionRequest request) {
        log.debug("Solicitud de creación de mensaje en bitácora para usuario ID: {}", request.getUsuarioId());
        BitacoraComunicacionResponse response = bitacoraService.crearMensaje(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/leido")
    public ResponseEntity<MensajeResponse> marcarComoLeido(@PathVariable Integer id) {
        log.debug("Solicitud de marcar mensaje ID: {} como leído", id);
        bitacoraService.marcarComoLeido(id);
        return ResponseEntity.ok(new MensajeResponse("Mensaje marcado como leído correctamente"));
    }

    // ==============================
    // CONSULTAS
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<BitacoraComunicacionResponse> obtenerPorId(@PathVariable Integer id) {
        log.debug("Solicitud de mensaje con ID: {}", id);
        BitacoraComunicacionResponse response = bitacoraService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BitacoraComunicacionResponse>> listarTodos() {
        log.debug("Solicitud de listado de todos los mensajes de la bitácora");
        List<BitacoraComunicacionResponse> responses = bitacoraService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/no-leidos")
    public ResponseEntity<List<BitacoraComunicacionResponse>> listarNoLeidos() {
        log.debug("Solicitud de mensajes no leídos");
        List<BitacoraComunicacionResponse> responses = bitacoraService.listarNoLeidos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<BitacoraComunicacionResponse>> listarPorUsuario(@PathVariable Integer usuarioId) {
        log.debug("Solicitud de mensajes del usuario ID: {}", usuarioId);
        List<BitacoraComunicacionResponse> responses = bitacoraService.listarPorUsuario(usuarioId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<BitacoraComunicacionResponse>> listarPorTipo(
            @PathVariable BitacoraComunicacion.Tipo tipo) {
        log.debug("Solicitud de mensajes por tipo: {}", tipo);
        List<BitacoraComunicacionResponse> responses = bitacoraService.listarPorTipo(tipo);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<BitacoraComunicacionResponse>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        log.debug("Solicitud de mensajes entre {} y {}", inicio, fin);
        List<BitacoraComunicacionResponse> responses = bitacoraService.listarPorFecha(inicio, fin);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/usuario/{usuarioId}/no-leidos")
    public ResponseEntity<List<BitacoraComunicacionResponse>> listarNoLeidosPorUsuario(
            @PathVariable Integer usuarioId) {
        log.debug("Solicitud de mensajes no leídos del usuario ID: {}", usuarioId);
        List<BitacoraComunicacionResponse> responses = bitacoraService.listarNoLeidosPorUsuario(usuarioId);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarMensaje(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de mensaje ID: {}", id);
        bitacoraService.eliminarMensaje(id);
        return ResponseEntity.ok(new MensajeResponse("Mensaje eliminado correctamente"));
    }
}