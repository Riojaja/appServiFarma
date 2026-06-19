package com.example.proyecto.app.service.impl;


import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.UsuarioResponse;
import com.example.proyecto.app.entity.Rol;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.UsuarioMapper;
import com.example.proyecto.app.repository.RolRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioResponse crearUsuario(UsuarioRequest request) {
        // Validar que el nombre de usuario no exista
        if (usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new DuplicadoException("El nombre de usuario '" + request.getUsuario() + "' ya está registrado.");
        }

        // Obtener el rol
        Rol rol = rolRepository.findById(request.getRolId())
                .orElseThrow(() -> new ResourceNotFoundException("Rol con ID " + request.getRolId() + " no encontrado."));

        // Mapear request a entidad
        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setRol(rol);

        // Encriptar la contraseña
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));

        // Guardar
        Usuario saved = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));

        // Validar que el nombre de usuario no esté siendo usado por otro usuario
        if (!usuario.getUsuario().equals(request.getUsuario()) &&
                usuarioRepository.existsByUsuario(request.getUsuario())) {
            throw new DuplicadoException("El nombre de usuario '" + request.getUsuario() + "' ya está registrado.");
        }

        // Actualizar campos básicos
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setUsuario(request.getUsuario());
        usuario.setActivo(request.getActivo());

        // Actualizar rol si cambia
        if (!usuario.getRol().getId().equals(request.getRolId())) {
            Rol nuevoRol = rolRepository.findById(request.getRolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rol con ID " + request.getRolId() + " no encontrado."));
            usuario.setRol(nuevoRol);
        }

        // No actualizamos la contraseña aquí, se hace en método separado
        Usuario updated = usuarioRepository.save(usuario);
        return usuarioMapper.toResponse(updated);
    }

    @Override
    public UsuarioResponse obtenerUsuarioPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    public UsuarioResponse obtenerUsuarioPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario '" + username + "' no encontrado."));
        return usuarioMapper.toResponse(usuario);
    }

    @Override
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> listarPorEstado(boolean activo) {
        return (activo ? usuarioRepository.findByActivoTrue() : usuarioRepository.findByActivoFalse())
                .stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> listarPorRol(Integer rolId) {
        // Verificar que el rol existe
        if (!rolRepository.existsById(rolId)) {
            throw new ResourceNotFoundException("Rol con ID " + rolId + " no encontrado.");
        }
        return usuarioRepository.findByRolId(rolId).stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsuarioResponse> buscarPorNombre(String nombre) {
        return usuarioRepository.findByNombreCompletoContainingIgnoreCase(nombre).stream()
                .map(usuarioMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cambiarEstadoUsuario(Integer id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void cambiarContrasena(Integer id, String nuevaContrasena) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + id + " no encontrado."));
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario con ID " + id + " no encontrado.");
        }
        usuarioRepository.deleteById(id);
    }
}