package com.example.proyecto.app.repository;

import com.example.proyecto.app.entity.SesionUsuario;
import com.example.proyecto.app.entity.Usuario;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SesionUsuarioRepository extends JpaRepository<SesionUsuario, Long> {
    Optional<SesionUsuario> findByTokenAndActivaTrue(String token);
    List<SesionUsuario> findByUsuarioAndActivaTrue(Usuario usuario);

    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.usuario.id = :usuarioId")
    void invalidarSesionesPorUsuario(@Param("usuarioId") Integer usuarioId);

    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.token = :token")
    void invalidarSesionPorToken(@Param("token") String token);

    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.fechaExpiracion < :fecha")
    void invalidarSesionesExpiradas(@Param("fecha") LocalDateTime fecha);

    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.activa = true AND s.ultimaActividad < :limite")
    void cerrarSesionesInactivas(@Param("limite") LocalDateTime limite);

    @Modifying
    @Transactional
    @Query("UPDATE SesionUsuario s SET s.activa = false WHERE s.activa = true AND s.usuario.rol.nombre = 'VENDEDOR'")
    void cerrarSesionesDeVendedores();

    List<SesionUsuario> findByActivaTrueAndFechaExpiracionBefore(LocalDateTime fecha);
}