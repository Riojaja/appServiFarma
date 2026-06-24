package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.FabricanteRequest;
import com.example.proyecto.app.dto.response.FabricanteResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.service.FabricanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/fabricantes")
@RequiredArgsConstructor
public class FabricanteController {

    private final FabricanteService fabricanteService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<FabricanteResponse> crearFabricante(@Valid @RequestBody FabricanteRequest request) {
        log.debug("Solicitud de creación de fabricante: {}", request.getNombre());
        FabricanteResponse response = fabricanteService.crearFabricante(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FabricanteResponse> actualizarFabricante(
            @PathVariable Integer id,
            @Valid @RequestBody FabricanteRequest request) {
        log.debug("Solicitud de actualización de fabricante con ID: {}", id);
        FabricanteResponse response = fabricanteService.actualizarFabricante(id, request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // OPERACIONES DE CONSULTA
    // ==============================

    @GetMapping
    public ResponseEntity<List<FabricanteResponse>> listarTodos() {
        log.debug("Solicitud de listado de todos los fabricantes");
        List<FabricanteResponse> responses = fabricanteService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FabricanteResponse> obtenerFabricantePorId(@PathVariable Integer id) {
        log.debug("Solicitud de fabricante con ID: {}", id);
        FabricanteResponse response = fabricanteService.obtenerFabricantePorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<FabricanteResponse>> buscarPorNombre(@RequestParam String nombre) {
        log.debug("Solicitud de búsqueda de fabricantes por nombre: {}", nombre);
        List<FabricanteResponse> responses = fabricanteService.buscarPorNombre(nombre);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorNombre(@RequestParam String nombre) {
        log.debug("Solicitud de verificación de existencia de fabricante con nombre: {}", nombre);
        boolean existe = fabricanteService.existePorNombre(nombre);
        return ResponseEntity.ok(existe);
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarFabricante(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de fabricante con ID: {}", id);
        fabricanteService.eliminarFabricante(id);
        return ResponseEntity.ok(new MensajeResponse("Fabricante eliminado correctamente"));
    }
}