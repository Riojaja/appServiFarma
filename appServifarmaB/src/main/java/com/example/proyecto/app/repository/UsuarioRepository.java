package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // ==============================
    // BÚSQUEDAS POR CAMPOS ÚNICOS
    // ==============================

    /**
     * Busca un usuario por su nombre de usuario (único).
     * Esencial para el proceso de autenticación (login).
     */
    Optional<Usuario> findByUsuario(String usuario);

    /**
     * Verifica si ya existe un usuario con el nombre de usuario dado.
     * Útil para validar duplicados al crear o actualizar usuarios.
     */
    boolean existsByUsuario(String usuario);

    // ==============================
    // FILTRADO POR ESTADO Y ROL
    // ==============================

    /**
     * Obtiene todos los usuarios activos (activo = true).
     */
    List<Usuario> findByActivoTrue();

    /**
     * Obtiene todos los usuarios inactivos (activo = false).
     */
    List<Usuario> findByActivoFalse();

    /**
     * Obtiene usuarios por rol (por ID de rol).
     * Útil para listar solo vendedores o administradores.
     */
    List<Usuario> findByRolId(Integer rolId);

    /**
     * Obtiene usuarios activos por rol (ID de rol y activo = true).
     */
    List<Usuario> findByRolIdAndActivoTrue(Integer rolId);

    // ==============================
    // BÚSQUEDAS POR TEXTO PARCIAL
    // ==============================

    /**
     * Busca usuarios por nombre completo (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Útil para autocompletado y búsquedas rápidas en la administración.
     */
    List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);

    // ==============================
    // LISTADOS ORDENADOS
    // ==============================

    /**
     * Obtiene todos los usuarios ordenados por nombre completo (A-Z).
     */
    List<Usuario> findAllByOrderByNombreCompletoAsc();

    /**
     * Obtiene todos los usuarios activos, ordenados por nombre completo (A-Z).
     */
    List<Usuario> findByActivoTrueOrderByNombreCompletoAsc();

    // ==============================
    // CONSULTAS PERSONALIZADAS (con @Query)
    // ==============================

    /**
     * Actualiza la contraseña de un usuario de manera eficiente.
     * Útil para el cambio de contraseña desde el perfil.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.contrasena = :nuevaContrasena WHERE u.id = :id")
    int updateContrasenaById(@Param("id") Integer id, @Param("nuevaContrasena") String nuevaContrasena);

    /**
     * Obtiene un usuario con su rol cargado (JOIN FETCH).
     * Útil para evitar problemas de LazyInitializationException en contextos fuera de transacción.
     */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.usuario = :usuario")
    Optional<Usuario> findByUsuarioWithRol(@Param("usuario") String usuario);

    /**
     * Cuenta cuántos usuarios hay por rol.
     * Útil para estadísticas de administración.
     */
    long countByRolId(Integer rolId);
}