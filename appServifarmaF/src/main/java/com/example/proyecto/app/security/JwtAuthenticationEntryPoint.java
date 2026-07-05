package com.example.proyecto.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Establecer el tipo de contenido de la respuesta como JSON
        response.setContentType("application/json");
        
        // Establecer el código de estado HTTP 401 (No autorizado)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Construir un mensaje de error en formato JSON para el cliente
        String jsonResponse = String.format(
                "{\"error\": \"No autorizado\", \"mensaje\": \"%s\"}",
                authException.getMessage() != null ? 
                        authException.getMessage() : 
                        "Acceso denegado. Token inválido o sesión expirada."
        );

        // Escribir la respuesta JSON en el cuerpo de la respuesta
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}