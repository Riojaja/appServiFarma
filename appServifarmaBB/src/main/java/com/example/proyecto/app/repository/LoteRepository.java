package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer> {

    // ==============================
    // MÉTODOS PARA LÓGICA FEFO
    // ==============================

    /**
     * Obtiene los lotes activos de un producto, ordenados por fecha de vencimiento (más próximo primero).
     * Esencial para la lógica FEFO (First Expired, First Out) al momento de vender.
     */
    List<Lote> findByProductoIdAndEstadoOrderByFechaVencimientoAsc(Integer productoId, Lote.EstadoLote estado);

    /**
     * Obtiene el lote más próximo a vencer (activo) de un producto.
     * Útil para seleccionar el lote a descontar en una venta.
     */
    Optional<Lote> findFirstByProductoIdAndEstadoOrderByFechaVencimientoAsc(Integer productoId, Lote.EstadoLote estado);

    // ==============================
    // CONSULTAS DE STOCK
    // ==============================

    /**
     * Obtiene todos los lotes activos de un producto.
     */
    List<Lote> findByProductoIdAndEstado(Integer productoId, Lote.EstadoLote estado);

    /**
     * Suma la cantidad total de un producto en lotes activos.
     * Retorna 0 si no hay lotes o todos están agotados/vencidos.
     */
    @Query("SELECT COALESCE(SUM(l.cantidad), 0) FROM Lote l WHERE l.producto.id = :productoId AND l.estado = :estado")
    Integer sumCantidadByProductoIdAndEstado(@Param("productoId") Integer productoId, @Param("estado") Lote.EstadoLote estado);

    /**
     * Verifica si un producto tiene al menos un lote activo con stock disponible.
     */
    boolean existsByProductoIdAndEstadoAndCantidadGreaterThan(Integer productoId, Lote.EstadoLote estado, int cantidadMinima);

    // ==============================
    // CONSULTAS POR ESTADO Y FECHAS
    // ==============================

    /**
     * Obtiene todos los lotes con un estado específico (activo, vencido, deteriorado, agotado).
     */
    List<Lote> findByEstado(Lote.EstadoLote estado);

    /**
     * Obtiene lotes con fecha de vencimiento anterior a la fecha actual (lotes vencidos).
     * Útil para tareas programadas que actualicen el estado a "vencido".
     */
    List<Lote> findByFechaVencimientoBeforeAndEstadoNot(LocalDate fecha, Lote.EstadoLote estadoExcluido);

    /**
     * Obtiene lotes con fecha de vencimiento en un rango (para alertas de vencimiento).
     * Ejemplo: lotes que vencen en los próximos 30 días.
     */
    List<Lote> findByFechaVencimientoBetweenAndEstado(LocalDate inicio, LocalDate fin, Lote.EstadoLote estado);

    /**
     * Obtiene todos los lotes de un proveedor específico.
     */
    List<Lote> findByProveedorId(Integer proveedorId);

    /**
     * Obtiene lotes por número de lote (coincidencia exacta).
     */
    Optional<Lote> findByLote(String lote);

    /**
     * Obtiene lotes cuyo número de lote contenga la cadena (búsqueda parcial).
     */
    List<Lote> findByLoteContaining(String lote);

    // ==============================
    // CONSULTAS PARA AUDITORÍA Y REPORTES
    // ==============================

    /**
     * Obtiene todos los lotes de un producto, ordenados por fecha de ingreso (más reciente primero).
     */
    List<Lote> findByProductoIdOrderByFechaIngresoDesc(Integer productoId);

    /**
     * Obtiene la cantidad total de lotes activos que tienen stock (cantidad > 0).
     */
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.estado = :estado AND l.cantidad > 0")
    Long countLotesActivosConStock(@Param("estado") Lote.EstadoLote estado);

    // ==============================
    // ACTUALIZACIONES MASIVAS (con @Modifying)
    // ==============================

    /**
     * Actualiza el estado de un lote a "agotado" cuando la cantidad llega a 0.
     * Se usa en el servicio de ventas después de descontar stock.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Lote l SET l.estado = :nuevoEstado WHERE l.id = :loteId AND l.cantidad = 0")
    int actualizarEstadoAAgotado(@Param("loteId") Integer loteId, @Param("nuevoEstado") Lote.EstadoLote nuevoEstado);

    /**
     * Actualiza el estado de todos los lotes con fecha de vencimiento anterior a la fecha actual a "vencido".
     * Útil para tareas programadas (batch) que se ejecuten diariamente.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Lote l SET l.estado = :estadoVencido WHERE l.fechaVencimiento < :fechaActual AND l.estado != :estadoVencido")
    int marcarLotesVencidos(@Param("fechaActual") LocalDate fechaActual, @Param("estadoVencido") Lote.EstadoLote estadoVencido);
}
