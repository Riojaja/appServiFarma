package com.example.proyecto.app.security;

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

import static com.example.proyecto.app.security.JwtConstants.HEADER_AUTHORIZATION;
import static com.example.proyecto.app.security.JwtConstants.TOKEN_PREFIX;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Obtener el token del header Authorization
            String token = getTokenFromRequest(request);

            // 2. Validar que el token exista y extraer información
            if (token != null) {
                // 3. Extraer el username del token
                String username = jwtUtil.extractUsername(token);

                // 4. Verificar que no haya una autenticación previa en el contexto
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 5. Cargar los detalles del usuario desde la BD
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // 6. Validar el token contra el usuario (verifica que el subject coincida y no esté expirado)
                    if (jwtUtil.validateToken(token, username)) {
                        // 7. Crear el objeto de autenticación con los roles/permisos del usuario
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        // 8. Agregar detalles de la petición (IP, sesión, etc.)
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 9. Establecer la autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("Usuario autenticado exitosamente: {}", username);
                    } else {
                        log.warn("Token JWT inválido o expirado para usuario: {}", username);
                    }
                }
            }
        } catch (Exception e) {
            // Si hay algún error (token inválido, usuario no encontrado, etc.),
            // simplemente logueamos y dejamos que el EntryPoint maneje el 401.
            // No lanzamos la excepción para no romper el flujo de la petición.
            log.error("Error al procesar el token JWT: {}", e.getMessage());
        }

        // 10. Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * Espera el formato: "Bearer <token>"
     *
     * @param request Petición HTTP
     * @return Token JWT sin el prefijo, o null si no está presente o no tiene el formato esperado.
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(HEADER_AUTHORIZATION);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }

        return null;
    }
}
