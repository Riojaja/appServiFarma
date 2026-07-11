package com.example.proyecto.app.util;

import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene el usuario autenticado actualmente.
     * @return Usuario autenticado
     * @throws ResourceNotFoundException si el usuario no se encuentra en la base de datos
     */
    public Usuario getUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado");
        }
        String username = authentication.getName();
        return usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Obtiene el ID del usuario autenticado.
     * @return ID del usuario autenticado
     */
    public Integer getUsuarioIdAutenticado() {
        return getUsuarioAutenticado().getId();
    }

    /**
     * Verifica si el usuario autenticado tiene un rol específico.
     * @param rol Nombre del rol (ej. "admin")
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    public boolean hasRole(String rol) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol.toUpperCase()));
    }
}