package com.example.proyecto.app.service;

import com.example.proyecto.app.entity.TokenInvalidado;
import com.example.proyecto.app.repository.TokenInvalidadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final TokenInvalidadoRepository tokenInvalidadoRepository;

    /**
     * Verifica si un token específico está en la blacklist.
     */
    public boolean isTokenBlacklisted(String token) {
        return tokenInvalidadoRepository.existsByToken(token);
    }

    /**
     * Verifica si el usuario tiene algún token invalidado (para invalidación por usuario).
     * Busca el registro especial "USER_INVALIDATED_<id>" o cualquier token del usuario.
     */
    public boolean isUserBlacklisted(Integer usuarioId) {
        String tokenEspecial = "USER_INVALIDATED_" + usuarioId;
        return tokenInvalidadoRepository.existsByToken(tokenEspecial);
    }

    /**
     * Invalida un token específico (para cierre de sesión voluntario).
     */
    @Transactional
    public void invalidateToken(String token, Integer usuarioId) {
        TokenInvalidado invalidado = TokenInvalidado.builder()
                .token(token)
                .usuarioId(usuarioId)
                .build();
        tokenInvalidadoRepository.save(invalidado);
        log.info("Token invalidado para usuario ID: {}", usuarioId);
    }

    /**
     * Invalida TODOS los tokens de un usuario (para cierre de sesión forzado por administrador).
     */
    @Transactional
    public void invalidateAllTokensByUser(Integer usuarioId) {
        // 1. Eliminar tokens antiguos de la blacklist para este usuario
        tokenInvalidadoRepository.deleteAllByUsuarioId(usuarioId);
        
        // 2. Guardar un registro especial que indique que el usuario está invalidado
        String tokenEspecial = "USER_INVALIDATED_" + usuarioId;
        TokenInvalidado invalidado = TokenInvalidado.builder()
                .token(tokenEspecial)
                .usuarioId(usuarioId)
                .build();
        tokenInvalidadoRepository.save(invalidado);
        
        log.info("✅ Todos los tokens del usuario ID: {} han sido invalidados", usuarioId);
    }
}