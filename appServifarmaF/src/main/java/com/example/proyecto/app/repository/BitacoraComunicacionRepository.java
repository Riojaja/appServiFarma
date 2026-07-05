package com.example.proyecto.app.repository;

import com.example.proyecto.app.entity.BitacoraComunicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BitacoraComunicacionRepository extends JpaRepository<BitacoraComunicacion, Integer> {

    /**
     * Obtiene todos los mensajes no leídos.
     */
    @Query("SELECT b FROM BitacoraComunicacion b WHERE b.leido = FALSE")
    List<BitacoraComunicacion> findByLeidoFalse();

    /**
     * Obtiene todos los mensajes de un usuario específico.
     */
    @Query("SELECT b FROM BitacoraComunicacion b WHERE b.usuario.id = :usuarioId")
    List<BitacoraComunicacion> findByUsuarioId(Integer usuarioId);

    /**
     * Obtiene mensajes por tipo (novedad, recordatorio, incidencia).
     */
    List<BitacoraComunicacion> findByTipo(BitacoraComunicacion.Tipo tipo);

    /**
     * Obtiene mensajes no leídos de un usuario específico.
     */
    List<BitacoraComunicacion> findByUsuarioIdAndLeidoFalse(Integer usuarioId);

    /**
     * Obtiene mensajes por rango de fechas (para consultar los del día, turno, etc.).
     */
    List<BitacoraComunicacion> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene los mensajes más recientes (ordenados por fecha descendente).
     */
    List<BitacoraComunicacion> findAllByOrderByFechaHoraDesc();
}