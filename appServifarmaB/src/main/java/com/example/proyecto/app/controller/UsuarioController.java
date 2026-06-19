package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.UsuarioResponse;
import com.example.proyecto.app.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crearUsuario(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.actualizarUsuario(id, request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // OPERACIONES DE CONSULTA
    // ==============================

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Integer id) {
        UsuarioResponse response = usuarioService.obtenerUsuarioPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorUsername(@PathVariable String username) {
        UsuarioResponse response = usuarioService.obtenerUsuarioPorUsername(username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarTodos(
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) Integer rolId) {
        
        if (activo != null) {
            return ResponseEntity.ok(usuarioService.listarPorEstado(activo));
        }
        
        if (rolId != null) {
            return ResponseEntity.ok(usuarioService.listarPorRol(rolId));
        }
        
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioResponse>> buscarPorNombre(@RequestParam String nombre) {
        List<UsuarioResponse> responses = usuarioService.buscarPorNombre(nombre);
        return ResponseEntity.ok(responses);
    }

    // ==============================
    // OPERACIONES DE MODIFICACIÓN DE ESTADO Y CONTRASEÑA
    // ==============================

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MensajeResponse> cambiarEstadoUsuario(
            @PathVariable Integer id,
            @RequestParam boolean activo) {
        usuarioService.cambiarEstadoUsuario(id, activo);
        String mensaje = activo ? "Usuario activado correctamente" : "Usuario desactivado correctamente";
        return ResponseEntity.ok(new MensajeResponse(mensaje));
    }

    @PatchMapping("/{id}/contrasena")
    public ResponseEntity<MensajeResponse> cambiarContrasena(
            @PathVariable Integer id,
            @RequestParam String nuevaContrasena) {
        usuarioService.cambiarContrasena(id, nuevaContrasena);
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente"));
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente"));
    }
}
