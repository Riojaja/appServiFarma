package com.example.proyecto.app.controller;

import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.MensajeResponse;
import com.example.proyecto.app.dto.response.UsuarioResponse;
import com.example.proyecto.app.service.ConfiguracionService;
import com.example.proyecto.app.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ConfiguracionService configuracionService; // inyectado

    // ========== CRUD ==========
    @PostMapping
    public ResponseEntity<UsuarioResponse> crearUsuario(@Valid @RequestBody UsuarioRequest request) {
        return new ResponseEntity<>(usuarioService.crearUsuario(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarUsuario(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorUsername(@PathVariable String username) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorUsername(username));
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
        return ResponseEntity.ok(usuarioService.buscarPorNombre(nombre));
    }

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

    @DeleteMapping("/{id}")
    public ResponseEntity<MensajeResponse> eliminarUsuario(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(new MensajeResponse("Usuario eliminado correctamente"));
    }

    // ========== GESTIÓN DE SESIONES ==========

    @PostMapping("/{id}/cerrar-sesion")
    public ResponseEntity<MensajeResponse> cerrarSesionUsuario(@PathVariable Integer id) {
        usuarioService.cerrarSesionesPorUsuario(id);
        return ResponseEntity.ok(new MensajeResponse("Sesiones del usuario cerradas correctamente"));
    }

    @PostMapping("/cerrar-sesiones-turno")
    public ResponseEntity<MensajeResponse> cerrarSesionesPorTurno() {
        usuarioService.cerrarSesionesPorTurno();
        return ResponseEntity.ok(new MensajeResponse("Cierre de sesiones por turno ejecutado correctamente"));
    }

    // ========== CONFIGURACIÓN DE SEGURIDAD (solo admin) ==========
    @GetMapping("/configuracion/seguridad")
    public ResponseEntity<Map<String, String>> obtenerConfiguracionSeguridad() {
        return ResponseEntity.ok(configuracionService.obtenerTodas());
    }

    @PutMapping("/configuracion/seguridad")
    public ResponseEntity<MensajeResponse> actualizarConfiguracionSeguridad(
            @RequestBody Map<String, String> configuraciones) {
        configuracionService.actualizarConfiguracion(configuraciones);
        return ResponseEntity.ok(new MensajeResponse("Configuración de seguridad actualizada correctamente"));
    }
}