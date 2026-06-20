package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Integer> {

    // ==============================
    // FILTRADO POR ENTIDADES RELACIONADAS
    // ==============================

    /**
     * Obtiene todos los movimientos de un lote específico (historial completo).
     */
    List<MovimientoStock> findByLoteId(Integer loteId);

    /**
     * Obtiene todos los movimientos realizados por un usuario específico.
     */
    List<MovimientoStock> findByUsuarioId(Integer usuarioId);

    /**
     * Obtiene movimientos por tipo (compra, venta, ajuste, merma).
     */
    List<MovimientoStock> findByTipoMovimiento(MovimientoStock.TipoMovimiento tipoMovimiento);

    /**
     * Obtiene movimientos de un lote, ordenados por fecha descendente (más reciente primero).
     */
    List<MovimientoStock> findByLoteIdOrderByFechaDesc(Integer loteId);

    // ==============================
    // FILTRADO POR FECHAS (REPORTES)
    // ==============================

    /**
     * Obtiene movimientos en un rango de fechas.
     * Útil para reportes diarios, semanales o mensuales.
     */
    List<MovimientoStock> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene movimientos de un tipo específico en un rango de fechas.
     */
    List<MovimientoStock> findByTipoMovimientoAndFechaBetween(MovimientoStock.TipoMovimiento tipoMovimiento, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene movimientos de un lote en un rango de fechas.
     */
    List<MovimientoStock> findByLoteIdAndFechaBetween(Integer loteId, LocalDateTime inicio, LocalDateTime fin);

    // ==============================
    // CONSULTAS DE AGREGACIÓN (SUM, COUNT, AVG)
    // ==============================

    /**
     * Suma la cantidad total de movimientos de un tipo específico para un lote.
     * Ejemplo: total de unidades vendidas de un lote.
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoStock m WHERE m.lote.id = :loteId AND m.tipoMovimiento = :tipo")
    Integer sumCantidadByLoteIdAndTipoMovimiento(@Param("loteId") Integer loteId, @Param("tipo") MovimientoStock.TipoMovimiento tipo);

    /**
     * Suma la cantidad total de movimientos de un tipo específico en un rango de fechas.
     * Útil para reportes: total de ventas, total de mermas, etc.
     */
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoStock m WHERE m.tipoMovimiento = :tipo AND m.fecha BETWEEN :inicio AND :fin")
    Integer sumCantidadByTipoMovimientoAndFechaBetween(@Param("tipo") MovimientoStock.TipoMovimiento tipo, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /**
     * Obtiene el costo unitario promedio ponderado de un lote, basado en los movimientos de compra.
     * Útil para calcular el costo promedio de un lote si ha tenido ajustes.
     */
    @Query("SELECT COALESCE(AVG(m.costoUnitario), 0) FROM MovimientoStock m WHERE m.lote.id = :loteId AND m.tipoMovimiento = 'compra'")
    BigDecimal avgCostoCompraByLoteId(@Param("loteId") Integer loteId);

    /**
     * Cuenta el número de movimientos de un tipo específico en un rango de fechas.
     * Útil para estadísticas de operaciones.
     */
    long countByTipoMovimientoAndFechaBetween(MovimientoStock.TipoMovimiento tipo, LocalDateTime inicio, LocalDateTime fin);

    // ==============================
    // CONSULTAS DE AUDITORÍA Y TRAZABILIDAD
    // ==============================

    /**
     * Obtiene movimientos por referencia_id (ej. ID de venta, ID de ajuste).
     * Útil para rastrear el origen de un movimiento.
     */
    List<MovimientoStock> findByReferenciaId(Integer referenciaId);

    /**
     * Obtiene movimientos con observación que contenga una cadena específica (búsqueda parcial).
     */
    List<MovimientoStock> findByObservacionContainingIgnoreCase(String observacion);
}
