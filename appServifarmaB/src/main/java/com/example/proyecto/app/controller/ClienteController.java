package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.ClienteRequest;
import com.example.proyecto.app.dto.response.ClienteResponse;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.entity.Cliente;
import com.example.proyecto.app.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<ClienteResponse> crearCliente(@Valid @RequestBody ClienteRequest request) {
        log.debug("Solicitud de creación de cliente: {}", request.getDocumentoNumero());
        ClienteResponse response = clienteService.crearCliente(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizarCliente(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request) {
        log.debug("Solicitud de actualización de cliente con ID: {}", id);
        ClienteResponse response = clienteService.actualizarCliente(id, request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // OPERACIONES DE CONSULTA
    // ==============================

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        log.debug("Solicitud de listado de todos los clientes");
        List<ClienteResponse> responses = clienteService.listarTodos();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtenerClientePorId(@PathVariable Integer id) {
        log.debug("Solicitud de cliente con ID: {}", id);
        ClienteResponse response = clienteService.obtenerClientePorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/documento/{documento}")
    public ResponseEntity<ClienteResponse> obtenerClientePorDocumento(@PathVariable String documento) {
        log.debug("Solicitud de cliente con documento: {}", documento);
        ClienteResponse response = clienteService.obtenerClientePorDocumento(documento);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscarPorNombre(@RequestParam String nombre) {
        log.debug("Solicitud de búsqueda de clientes por nombre: {}", nombre);
        List<ClienteResponse> responses = clienteService.buscarPorNombre(nombre);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/tipo")
    public ResponseEntity<List<ClienteResponse>> buscarPorDocumentoTipo(@RequestParam Cliente.DocumentoTipo tipo) {
        log.debug("Solicitud de búsqueda de clientes por tipo de documento: {}", tipo);
        List<ClienteResponse> responses = clienteService.buscarPorDocumentoTipo(tipo);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> existePorDocumento(@RequestParam String documento) {
        log.debug("Solicitud de verificación de existencia de cliente con documento: {}", documento);
        boolean existe = clienteService.existePorDocumento(documento);
        return ResponseEntity.ok(existe);
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarCliente(@PathVariable Integer id) {
        log.debug("Solicitud de eliminación de cliente con ID: {}", id);
        clienteService.eliminarCliente(id);
        return ResponseEntity.ok(new MensajeResponse("Cliente eliminado correctamente"));
    }
}