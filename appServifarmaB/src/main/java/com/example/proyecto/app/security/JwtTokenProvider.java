package com.example.proyecto.app.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtTokenProvider {
    // Blacklist de tokens inválidos (en memoria)
    private final Set<String> tokenBlacklist = ConcurrentHashMap.newKeySet();

    // Map para guardar tokens activos por usuario
    private final Map<Integer, Set<String>> userTokens = new ConcurrentHashMap<>();

    /**
     * Invalida todos los tokens de un usuario (cierra todas sus sesiones).
     */
    public void invalidateTokensForUser(Integer userId) {
        Set<String> tokens = userTokens.remove(userId);
        if (tokens != null) {
            tokenBlacklist.addAll(tokens);
        }
    }

    /**
     * Registra un token activo para un usuario.
     */
    public void registerToken(Integer userId, String token) {
        userTokens.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(token);
    }

    /**
     * Verifica si un token está en la blacklist.
     */
    public boolean isTokenInvalidated(String token) {
        return tokenBlacklist.contains(token);
    }

    // ... otros métodos de generación/validación de tokens
}