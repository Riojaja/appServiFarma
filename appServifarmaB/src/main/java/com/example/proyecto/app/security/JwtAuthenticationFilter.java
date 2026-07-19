package com.example.proyecto.app.security;

import com.example.proyecto.app.entity.SesionUsuario;
import com.example.proyecto.app.repository.SesionUsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

import static com.example.proyecto.app.security.JwtConstants.HEADER_AUTHORIZATION;
import static com.example.proyecto.app.security.JwtConstants.TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final SesionUsuarioRepository sesionRepository; // ⬅️ Reemplaza TokenBlacklistService

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            
            if (token != null && !token.trim().isEmpty()) {
                
                // 🔥 1. Verificar que la sesión exista y esté activa
                SesionUsuario sesion = sesionRepository.findByTokenAndActivaTrue(token).orElse(null);
                if (sesion == null) {
                    log.warn("❌ Sesión no encontrada o inactiva para token: {}", token.substring(0, Math.min(token.length(), 10)) + "...");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"mensaje\":\"Sesión cerrada o inválida\"}");
                    return;
                }

                // 🔥 2. Extraer username del token
                String username = jwtUtil.extractUsername(token);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // 🔥 3. Cargar UserDetails
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 🔥 4. Validar token
                    if (jwtUtil.validateToken(token, username)) {
                        
                        // 🔥 5. Actualizar última actividad
                        sesion.setUltimaActividad(LocalDateTime.now());
                        sesionRepository.save(sesion);
                        
                        // 🔥 6. Construir autenticación
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("✅ Usuario autenticado exitosamente: {} (sesión ID: {})", username, sesion.getId());
                    } else {
                        log.warn("⚠️ Token JWT inválido o expirado para usuario: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Error al procesar el token JWT: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }
}