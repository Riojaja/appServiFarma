package com.example.proyecto.app.auth;

import com.example.proyecto.app.entity.SesionUsuario;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.repository.SesionUsuarioRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SesionUsuarioRepository sesionRepository; // ⬅️ Nuevo

    @Override
    @Transactional  // ⬅️ CAMBIADO: quitamos readOnly para poder guardar
    public AuthResponse autenticar(LoginRequest request) {
        log.debug("Intento de autenticación para usuario: {}", request.getUsuario());

        // 1. Buscar usuario
        Usuario usuario = usuarioRepository.findByUsuario(request.getUsuario())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", request.getUsuario());
                    return new AuthException("Usuario o contraseña incorrectos");
                });

        // 2. Verificar activo
        if (!usuario.getActivo()) {
            log.warn("Usuario inactivo: {}", request.getUsuario());
            throw new AuthException("Usuario inactivo. Contacte al administrador.");
        }

        // 3. Verificar contraseña
        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            log.warn("Contraseña incorrecta para usuario: {}", request.getUsuario());
            throw new AuthException("Usuario o contraseña incorrectos");
        }

        // 4. Generar token
        int expirationSeconds = 3600; // 1 hora
        String token = jwtUtil.generateToken(usuario.getUsuario(), expirationSeconds);

        // 🔥 5. Obtener IP y dispositivo de la petición
        HttpServletRequest servletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 255) {
            userAgent = userAgent.substring(0, 255); // Cortar si es muy largo
        }

        // 🔥 6. Guardar sesión en la tabla
        SesionUsuario sesion = SesionUsuario.builder()
                .token(token)
                .activa(true)
                .ip(ip != null ? ip : "0.0.0.0")
                .dispositivo(userAgent != null ? userAgent : "Desconocido")
                .fechaInicio(LocalDateTime.now())
                .fechaExpiracion(LocalDateTime.now().plusSeconds(expirationSeconds))
                .ultimaActividad(LocalDateTime.now())
                .usuario(usuario)
                .build();
        sesionRepository.save(sesion);

        log.info("✅ Autenticación exitosa para usuario: {} (sesión ID: {})", 
                  request.getUsuario(), sesion.getId());

        // 7. Construir respuesta
        return AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .refreshToken(null)
                .usuario(usuario.getUsuario())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().getNombre().toUpperCase())
                .build();
    }

    @Override
    public AuthResponse refrescarToken(String refreshToken) {
        log.warn("Intento de refresco de token no implementado");
        throw new UnsupportedOperationException("Refresh token aún no implementado");
    }

    @Override
    @Transactional
    public void cambiarContrasena(ChangePasswordRequest request, String nombreUsuario) {
        log.debug("Solicitud de cambio de contraseña para usuario: {}", nombreUsuario);

        Usuario usuario = usuarioRepository.findByUsuario(nombreUsuario)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado para cambio de contraseña: {}", nombreUsuario);
                    return new AuthException("Usuario no encontrado");
                });

        if (!passwordEncoder.matches(request.getContrasenaActual(), usuario.getContrasena())) {
            log.warn("Contraseña actual incorrecta para usuario: {}", nombreUsuario);
            throw new AuthException("La contraseña actual es incorrecta");
        }

        if (!request.getNuevaContrasena().equals(request.getConfirmacionContrasena())) {
            log.warn("Nueva contraseña y confirmación no coinciden para usuario: {}", nombreUsuario);
            throw new AuthException("La nueva contraseña y la confirmación no coinciden");
        }

        if (request.getNuevaContrasena().length() < 6) {
            log.warn("Nueva contraseña muy corta para usuario: {}", nombreUsuario);
            throw new AuthException("La nueva contraseña debe tener al menos 6 caracteres");
        }

        usuario.setContrasena(passwordEncoder.encode(request.getNuevaContrasena()));
        usuarioRepository.save(usuario);
        log.info("Contraseña actualizada exitosamente para usuario: {}", nombreUsuario);
    }

    @Override
    @Transactional  // ⬅️ Agregamos @Transactional para que guarde en BD
    public void cerrarSesion(String token) {
        log.debug("Cierre de sesión solicitado para token: {}", 
                  token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null");

        if (token != null && !token.trim().isEmpty()) {
            // 🔥 Invalidar la sesión en la base de datos
            sesionRepository.invalidarSesionPorToken(token);
            log.info("✅ Sesión invalidada correctamente para token: {}", 
                     token.substring(0, Math.min(token.length(), 10)) + "...");
        } else {
            log.warn("⚠️ Token nulo o vacío al intentar cerrar sesión");
        }
    }
}