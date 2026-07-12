package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    // ========== CRUD ==========
    UsuarioResponse crearUsuario(UsuarioRequest request);
    UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request);
    UsuarioResponse obtenerUsuarioPorId(Integer id);
    UsuarioResponse obtenerUsuarioPorUsername(String username);
    List<UsuarioResponse> listarTodos();
    List<UsuarioResponse> listarPorEstado(boolean activo);
    List<UsuarioResponse> listarPorRol(Integer rolId);
    List<UsuarioResponse> buscarPorNombre(String nombre);
    void cambiarEstadoUsuario(Integer id, boolean activo);
    void cambiarContrasena(Integer id, String nuevaContrasena);
    void eliminarUsuario(Integer id);

    // ========== NUEVOS MÉTODOS PARA SESIONES ==========
    void cerrarSesionesPorTurno();
    void cerrarSesionesPorUsuario(Integer usuarioId);
}