package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.ParametroSistemaRequest;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.ParametroSistemaResponse;
import com.example.proyecto.app.service.ParametroSistemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
@RequiredArgsConstructor
public class ParametroSistemaController {

    private final ParametroSistemaService parametroSistemaService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<ParametroSistemaResponse> crearParametro(
            @Valid @RequestBody ParametroSistemaRequest request) {
        ParametroSistemaResponse response = parametroSistemaService.crearParametro(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParametroSistemaResponse> actualizarParametro(
            @PathVariable Integer id,
            @Valid @RequestBody ParametroSistemaRequest request) {
        ParametroSistemaResponse response = parametroSistemaService.actualizarParametro(id, request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // OPERACIONES DE CONSULTA
    // ==============================

    @GetMapping
    public ResponseEntity<List<ParametroSistemaResponse>> listarTodos() {
        List<ParametroSistemaResponse> responses = parametroSistemaService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParametroSistemaResponse> obtenerPorId(@PathVariable Integer id) {
        ParametroSistemaResponse response = parametroSistemaService.obtenerPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clave/{clave}")
    public ResponseEntity<ParametroSistemaResponse> obtenerPorClave(@PathVariable String clave) {
        ParametroSistemaResponse response = parametroSistemaService.obtenerPorClave(clave);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/clave/{clave}/valor")
    public ResponseEntity<String> obtenerValorPorClave(@PathVariable String clave) {
        String valor = parametroSistemaService.obtenerValorPorClave(clave);
        return ResponseEntity.ok(valor);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorClave(@RequestParam String clave) {
        boolean existe = parametroSistemaService.existePorClave(clave);
        return ResponseEntity.ok(existe);
    }

    // ==============================
    // OPERACIONES DE ACTUALIZACIÓN RÁPIDA
    // ==============================

    @PatchMapping("/clave/{clave}/valor")
    public ResponseEntity<MensajeResponse> actualizarValorPorClave(
            @PathVariable String clave,
            @RequestParam String nuevoValor) {
        parametroSistemaService.actualizarValorPorClave(clave, nuevoValor);
        return ResponseEntity.ok(new MensajeResponse("Valor actualizado correctamente"));
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarParametro(@PathVariable Integer id) {
        parametroSistemaService.eliminarParametro(id);
        return ResponseEntity.ok(new MensajeResponse("Parámetro eliminado correctamente"));
    }
}
