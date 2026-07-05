package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.ProveedorRequest;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.ProveedorResponse;
import com.example.proyecto.app.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<ProveedorResponse> crearProveedor(@Valid @RequestBody ProveedorRequest request) {
        log.debug("Solicitud de creación de proveedor: {}", request.getRuc());
        ProveedorResponse response = proveedorService.crearProveedor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponse> actualizarProveedor(
            @PathVariable Integer id,
            @Valid @RequestBody ProveedorRequest request) {
        log.debug("Solicitud de actualización de proveedor con ID: {}", id);
        ProveedorResponse response = proveedorService.actualizarProveedor(id, request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // OPERACIONES DE CONSULTA
    // ==============================

    @GetMapping
    public ResponseEntity<List<ProveedorResponse>> listarTodos() {
        log.debug("Solicitud de listado de todos los proveedores");
        List<ProveedorResponse> responses = proveedorService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponse> obtenerProveedorPorId(@PathVariable Integer id) {
        log.debug("Solicitud de proveedor con ID: {}", id);
        ProveedorResponse response = proveedorService.obtenerProveedorPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<ProveedorResponse> obtenerProveedorPorRuc(@PathVariable String ruc) {
        log.debug("Solicitud de proveedor con RUC: {}", ruc);
        ProveedorResponse response = proveedorService.obtenerProveedorPorRuc(ruc);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS (RF12)
    // ==============================

    @GetMapping("/buscar")
    public ResponseEntity<List<ProveedorResponse>> buscarPorRazonSocial(@RequestParam String razonSocial) {
        log.debug("Solicitud de búsqueda de proveedores por razón social: {}", razonSocial);
        List<ProveedorResponse> responses = proveedorService.buscarPorRazonSocial(razonSocial);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/buscar/contacto")
    public ResponseEntity<List<ProveedorResponse>> buscarPorContacto(@RequestParam String contacto) {
        log.debug("Solicitud de búsqueda de proveedores por contacto: {}", contacto);
        List<ProveedorResponse> responses = proveedorService.buscarPorContacto(contacto);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/region")
    public ResponseEntity<List<ProveedorResponse>> buscarPorRegion(@RequestParam String region) {
        log.debug("Solicitud de búsqueda de proveedores por región: {}", region);
        List<ProveedorResponse> responses = proveedorService.buscarPorRegion(region);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/regiones")
    public ResponseEntity<List<String>> obtenerRegionesDistintas() {
        log.debug("Solicitud de listado de regiones distintas");
        List<String> regiones = proveedorService.obtenerRegionesDistintas();
        return ResponseEntity.ok(regiones);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorRuc(@RequestParam String ruc) {
        log.debug("Solicitud de verificación de existencia de proveedor con RUC: {}", ruc);
        boolean existe = proveedorService.existePorRuc(ruc);
        return ResponseEntity.ok(existe);
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarProveedor(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de proveedor con ID: {}", id);
        proveedorService.eliminarProveedor(id);
        return ResponseEntity.ok(new MensajeResponse("Proveedor eliminado correctamente"));
    }
}