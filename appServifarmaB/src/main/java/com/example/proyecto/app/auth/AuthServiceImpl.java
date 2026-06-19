package com.example.proyecto.app.auth;

import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse autenticar(LoginRequest request) {
        log.debug("Intento de autenticación para usuario: {}", request.getUsuario());

        // 1. Buscar usuario por nombre de usuario
        Usuario usuario = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", request.getUsuario());
                    return new AuthException("Usuario o contraseña incorrectos");
                });

        // 2. Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            log.warn("Usuario inactivo: {}", request.getUsuario());
            throw new AuthException("Usuario inactivo. Contacte al administrador.");
        }

        // 3. Verificar contraseña
        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.getUsuario());
            throw new AuthException("Usuario o contraseña incorrectos");
        }

        // 4. Generar token JWT
        String token = jwtUtil.generateToken(usuario.getUsuario());
        log.info("Autenticación exitosa para usuario: {}", request.getUsuario());

        // 5. Construir respuesta
        return AuthResponse.builder()
                .token(token)
                .refreshToken(null) // Opcional: si implementas refresh token, aquí lo generarías
                .usuario(usuario.getUsuario())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().getNombre().toUpperCase())
                .build();
    }

    @Override
    public AuthResponse refrescarToken(String refreshToken) {
        // Validar que el refresh token sea válido (pendiente de implementación)
        // Por ahora, este método solo devuelve un nuevo token si el refresh token es válido.
        // Si decides implementarlo, necesitarías una lógica similar a la de autenticar,
        // pero usando un token de refresco almacenado (ej. en base de datos o en el mismo token).
        log.warn("Intento de refresco de token no implementado");
        throw new UnsupportedOperationException("Refresh token aún no implementado");
    }

    @Override
    @Transactional
    public void cambiarContrasena(ChangePasswordRequest request, String nombreUsuario) {
        log.debug("Solicitud de cambio de contraseña para usuario: {}", nombreUsuario);

        // 1. Buscar usuario autenticado por su nombre
        Usuario usuario = usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado para cambio de contraseña: {}", nombreUsuario);
                    return new AuthException("Usuario no encontrado");
                });

        // 2. Verificar contraseña actual
        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            log.warn("Contraseña actual incorrecta para usuario: {}", nombreUsuario);
            throw new AuthException("La contraseña actual es incorrecta");
        }

        // 3. Verificar que la nueva contraseña y la confirmación coincidan
        if (!request.getNuevaContrasena().equals(request.getConfirmacionContrasena())) {
            log.warn("Nueva contraseña y confirmación no coinciden para usuario: {}", nombreUsuario);
            throw new AuthException("La nueva contraseña y la confirmación no coinciden");
        }

        // 4. Validar longitud mínima
        if (request.getNuevaContrasena().length() < 6) {
            log.warn("Nueva contraseña muy corta para usuario: {}", nombreUsuario);
            throw new AuthException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        // 5. Actualizar contraseña
        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        log.info("Contraseña actualizada exitosamente para usuario: {}", nombreUsuario);
    }

    @Override
    public void cerrarSesion(String token) {
        // Si se implementa una blacklist o invalidación de tokens, aquí se agregaría.
        // Por ahora, no hay acción a realizar porque JWT es stateless.
        // En el frontend, simplemente se elimina el token del almacenamiento local.
        log.debug("Cierre de sesión solicitado para token: {}", token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null");
    }
}