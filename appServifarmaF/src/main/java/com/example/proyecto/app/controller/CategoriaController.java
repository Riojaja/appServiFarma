package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.CategoriaRequest;
import com.example.proyecto.app.dto.response.CategoriaResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<CategoriaResponse> crearCategoria(@Valid @RequestBody CategoriaRequest request) {
        log.debug("Solicitud de creación de categoría: {}", request.getNombre());
        CategoriaResponse response = categoriaService.crearCategoria(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponse> actualizarCategoria(
            @PathVariable Integer id,
            @Valid @RequestBody CategoriaRequest request) {
        log.debug("Solicitud de actualización de categoría con ID: {}", id);
        CategoriaResponse response = categoriaService.actualizarCategoria(id, request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // OPERACIONES DE CONSULTA
    // ==============================

    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> listarTodas() {
        log.debug("Solicitud de listado de todas las categorías");
        List<CategoriaResponse> responses = categoriaService.listarTodas();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> obtenerCategoriaPorId(@PathVariable Integer id) {
        log.debug("Solicitud de categoría con ID: {}", id);
        CategoriaResponse response = categoriaService.obtenerCategoriaPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaResponse>> buscarPorNombre(@RequestParam String nombre) {
        log.debug("Solicitud de búsqueda de categorías por nombre: {}", nombre);
        List<CategoriaResponse> responses = categoriaService.buscarPorNombre(nombre);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorNombre(@RequestParam String nombre) {
        log.debug("Solicitud de verificación de existencia de categoría con nombre: {}", nombre);
        boolean existe = categoriaService.existePorNombre(nombre);
        return ResponseEntity.ok(existe);
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarCategoria(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de categoría con ID: {}", id);
        categoriaService.eliminarCategoria(id);
        return ResponseEntity.ok(new MensajeResponse("Categoría eliminada correctamente"));
    }
}