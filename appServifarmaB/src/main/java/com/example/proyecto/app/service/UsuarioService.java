package com.example.proyecto.app.service;


import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {

    /**
     * Crea un nuevo usuario en el sistema.
     */
    UsuarioResponse crearUsuario(UsuarioRequest request);

    /**
     * Actualiza los datos de un usuario existente (excepto la contraseña).
     */
    UsuarioResponse actualizarUsuario(Integer id, UsuarioRequest request);

    /**
     * Obtiene un usuario por su ID.
     */
    UsuarioResponse obtenerUsuarioPorId(Integer id);

    /**
     * Obtiene un usuario por su nombre de usuario (para validaciones o detalles).
     */
    UsuarioResponse obtenerUsuarioPorUsername(String username);

    /**
     * Lista todos los usuarios registrados.
     */
    List<UsuarioResponse> listarTodos();

    /**
     * Lista usuarios por estado (activo/inactivo).
     */
    List<UsuarioResponse> listarPorEstado(boolean activo);

    /**
     * Lista usuarios por rol (por ID de rol).
     */
    List<UsuarioResponse> listarPorRol(Integer rolId);

    /**
     * Busca usuarios por nombre completo (coincidencia parcial, ignorando mayúsculas).
     */
    List<UsuarioResponse> buscarPorNombre(String nombre);

    /**
     * Cambia el estado (activo/inactivo) de un usuario.
     */
    void cambiarEstadoUsuario(Integer id, boolean activo);

    /**
     * Cambia la contraseña de un usuario.
     */
    void cambiarContrasena(Integer id, String nuevaContrasena);

    /**
     * Elimina un usuario del sistema (borrado físico).
     * Nota: Considera si prefieres solo desactivarlo (cambiar estado a false).
     */
    void eliminarUsuario(Integer id);
}
