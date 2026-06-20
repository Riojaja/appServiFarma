package com.example.proyecto.app.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.proyecto.app.dto.response.MensajeResponse;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ==============================
    // ENDPOINTS PÚBLICOS
    // ==============================

    /**
     * Inicia sesión y devuelve un token JWT.
     * Endpoint público (no requiere autenticación).
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.debug("Solicitud de login para usuario: {}", request.getUsuario());
        AuthResponse response = authService.autenticar(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Renueva el token JWT utilizando un refresh token.
     * Endpoint público (no requiere autenticación).
     * 
     * @throws UnsupportedOperationException Si el refresh token no está implementado.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Solicitud de refresco de token");
        AuthResponse response = authService.refrescarToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    // ==============================
    // ENDPOINTS PROTEGIDOS (requieren autenticación)
    // ==============================

    /**
     * Cambia la contraseña del usuario autenticado.
     * Requiere token JWT válido.
     */
    @PatchMapping("/cambiar-contrasena")
    public ResponseEntity<MensajeResponse> cambiarContrasena(@Valid @RequestBody ChangePasswordRequest request) {
        // Obtener el nombre de usuario del contexto de seguridad
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Intento de cambio de contraseña sin autenticación");
            return ResponseEntity.status(401).body(new MensajeResponse("No autenticado"));
        }
        
        String username = authentication.getName();
        log.debug("Solicitud de cambio de contraseña para usuario: {}", username);
        
        authService.cambiarContrasena(request, username);
        return ResponseEntity.ok(new MensajeResponse("Contraseña actualizada correctamente"));
    }

    /**
     * Cierra la sesión del usuario (invalida el token en el servidor si se implementa).
     * Requiere token JWT válido.
     */
    @PostMapping("/logout")
    public ResponseEntity<MensajeResponse> logout(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            authService.cerrarSesion(token);
            log.debug("Sesión cerrada para token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
        } else {
            log.warn("Intento de logout sin token Authorization");
        }
        return ResponseEntity.ok(new MensajeResponse("Sesión cerrada correctamente"));
    }
}