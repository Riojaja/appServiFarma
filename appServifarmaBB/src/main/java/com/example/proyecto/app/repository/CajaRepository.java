package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Caja;
import com.example.proyecto.app.entity.Caja.EstadoCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {

    /**
     * Obtiene la última caja abierta (ordenada por fecha de apertura descendente).
     * Útil para saber si hay una caja activa en el momento.
     */
    Optional<Caja> findFirstByEstadoOrderByFechaAperturaDesc(EstadoCaja estado);

    /**
     * Obtiene todas las cajas de un usuario específico que realizó la apertura.
     */
    List<Caja> findByUsuarioAperturaId(Integer usuarioAperturaId);

    /**
     * Verifica si un usuario tiene una caja abierta actualmente.
     */
    boolean existsByUsuarioAperturaIdAndEstado(Integer usuarioAperturaId, EstadoCaja estado);

    /**
     * Obtiene todas las cajas cerradas en un rango de fechas.
     */
    List<Caja> findByFechaCierreBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene todas las cajas ordenadas por fecha de apertura descendente (más recientes primero).
     */
    List<Caja> findAllByOrderByFechaAperturaDesc();

    /**
     * Obtiene todas las cajas con un estado específico (abiertas o cerradas).
     */
    List<Caja> findByEstado(EstadoCaja estado);

    /**
     * (Opcional) Consulta personalizada para obtener la caja abierta más reciente
     * junto con el total de ventas asociadas (usando un DTO o proyección).
     * Este método es un ejemplo de cómo podrías extenderlo si necesitas datos agregados.
     */
    @Query("SELECT c FROM Caja c LEFT JOIN FETCH c.ventas WHERE c.id = :id")
    Optional<Caja> findByIdWithVentas(@Param("id") Integer id);
}