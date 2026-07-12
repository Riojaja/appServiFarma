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

    // ========== BÚSQUEDAS BÁSICAS ==========
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);

    // ========== FILTROS POR ESTADO ==========
    List<Usuario> findByActivoTrue();
    List<Usuario> findByActivoFalse();
    List<Usuario> findByRolId(Integer rolId);
    List<Usuario> findByRolIdAndActivoTrue(Integer rolId);

    // ========== NUEVO: Buscar por nombre de rol + activo ==========
    @Query("SELECT u FROM Usuario u JOIN u.rol r WHERE r.nombre = :rolNombre AND u.activo = true")
    List<Usuario> findByRolNombreAndActivoTrue(@Param("rolNombre") String rolNombre);

    // ========== BÚSQUEDAS POR TEXTO ==========
    List<Usuario> findByNombreCompletoContainingIgnoreCase(String nombreCompleto);

    // ========== LISTADOS ORDENADOS ==========
    List<Usuario> findAllByOrderByNombreCompletoAsc();
    List<Usuario> findByActivoTrueOrderByNombreCompletoAsc();

    // ========== CONSULTAS PERSONALIZADAS ==========
    @Modifying
    @Transactional
    @Query("UPDATE Usuario u SET u.contrasena = :nuevaContrasena WHERE u.id = :id")
    int updateContrasenaById(@Param("id") Integer id, @Param("nuevaContrasena") String nuevaContrasena);

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol WHERE u.usuario = :usuario")
    Optional<Usuario> findByUsuarioWithRol(@Param("usuario") String usuario);

    long countByRolId(Integer rolId);
}