package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.entity.Venta.EstadoVenta;
import com.example.proyecto.app.entity.Venta.MedioPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    // ==============================
    // BÚSQUEDAS POR RELACIONES
    // ==============================

    /**
     * Obtiene todas las ventas de un usuario específico (vendedor).
     */
    List<Venta> findByUsuarioId(Integer usuarioId);

    /**
     * Obtiene todas las ventas de un cliente específico.
     * Útil para el historial de compras (requisito RF8 y RF29).
     */
    List<Venta> findByClienteId(Integer clienteId);

    /**
     * Obtiene todas las ventas de una caja específica.
     */
    List<Venta> findByCajaId(Integer cajaId);

    // ==============================
    // FILTRADO POR ESTADO
    // ==============================

    /**
     * Obtiene todas las ventas completadas.
     */
    List<Venta> findByEstado(EstadoVenta estado);

    /**
     * Obtiene todas las ventas completadas de una caja específica.
     * Útil para el cierre de caja (sumar solo ventas completadas).
     */
    List<Venta> findByCajaIdAndEstado(Integer cajaId, EstadoVenta estado);

    // ==============================
    // FILTRADO POR FECHAS (REPORTES)
    // ==============================

    /**
     * Obtiene ventas en un rango de fechas.
     * Útil para reportes diarios, semanales o mensuales.
     */
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene ventas completadas en un rango de fechas.
     */
    List<Venta> findByFechaBetweenAndEstado(LocalDateTime inicio, LocalDateTime fin, EstadoVenta estado);

    /**
     * Obtiene ventas por medio de pago en un rango de fechas.
     * Útil para estadísticas de medios de pago (requisito RF4 y RF12).
     */
    List<Venta> findByMedioPagoAndFechaBetween(MedioPago medioPago, LocalDateTime inicio, LocalDateTime fin);

    // ==============================
    // CONSULTAS DE AGREGACIÓN (SUM, COUNT, AVG)
    // ==============================

    /**
     * Suma el total de ventas de una caja específica (solo ventas completadas).
     * Esencial para el cierre de caja (RF25).
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.caja.id = :cajaId AND v.estado = :estado")
    BigDecimal sumTotalByCajaIdAndEstado(@Param("cajaId") Integer cajaId, @Param("estado") EstadoVenta estado);

    /**
     * Suma el total de ventas de un cliente específico (historial de compras).
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.cliente.id = :clienteId AND v.estado = :estado")
    BigDecimal sumTotalByClienteIdAndEstado(@Param("clienteId") Integer clienteId, @Param("estado") EstadoVenta estado);

    /**
     * Suma el total de ventas en un rango de fechas por medio de pago.
     * Útil para reportes de ingresos desglosados por método de pago (RF4).
     */
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.medioPago = :medioPago AND v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado")
    BigDecimal sumTotalByMedioPagoAndFechaBetween(@Param("medioPago") MedioPago medioPago,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fin") LocalDateTime fin,
                                                   @Param("estado") EstadoVenta estado);

    /**
     * Cuenta el número de ventas en un rango de fechas por medio de pago.
     * Útil para estadísticas de frecuencia de uso de cada medio de pago.
     */
    long countByMedioPagoAndFechaBetweenAndEstado(MedioPago medioPago, LocalDateTime inicio, LocalDateTime fin, EstadoVenta estado);

    // ==============================
    // CONSULTAS PARA REPORTES Y ESTADÍSTICAS
    // ==============================

    /**
     * Obtiene las ventas más recientes (limitadas a un número específico).
     * Útil para el panel de actividad en el dashboard.
     */
    @Query("SELECT v FROM Venta v WHERE v.estado = :estado ORDER BY v.fecha DESC")
    List<Venta> findTopVentasRecientes(@Param("estado") EstadoVenta estado);

    /**
     * Obtiene las ventas de un cliente con sus detalles (JOIN FETCH).
     * Útil para mostrar el historial de compras en el frontend sin LazyInitializationException.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.detalles d JOIN FETCH d.lote l JOIN FETCH l.producto p WHERE v.cliente.id = :clienteId AND v.estado = :estado")
    List<Venta> findByClienteIdWithDetalles(@Param("clienteId") Integer clienteId, @Param("estado") EstadoVenta estado);

    /**
     * Obtiene una venta con todos sus detalles cargados (JOIN FETCH).
     * Útil para mostrar el detalle completo de una boleta o factura.
     */
    @Query("SELECT v FROM Venta v JOIN FETCH v.detalles d JOIN FETCH d.lote l JOIN FETCH l.producto p WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Integer id);

    /**
     * Obtiene el total de ventas agrupado por día en un rango de fechas.
     * Útil para gráficos de tendencias de ventas.
     */
    @Query("SELECT DATE(v.fecha) as fecha, SUM(v.total) as total FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado " +
           "GROUP BY DATE(v.fecha) ORDER BY fecha ASC")
    List<Object[]> findTotalVentasAgrupadoPorDia(@Param("inicio") LocalDateTime inicio,
                                                   @Param("fin") LocalDateTime fin,
                                                   @Param("estado") EstadoVenta estado);

    /**
     * Obtiene el total de ventas agrupado por mes en un rango de fechas.
     * Útil para reportes anuales y análisis de tendencias (RF13).
     */
    @Query("SELECT YEAR(v.fecha) as anio, MONTH(v.fecha) as mes, SUM(v.total) as total FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado " +
           "GROUP BY YEAR(v.fecha), MONTH(v.fecha) ORDER BY anio ASC, mes ASC")
    List<Object[]> findTotalVentasAgrupadoPorMes(@Param("inicio") LocalDateTime inicio,
                                                   @Param("fin") LocalDateTime fin,
                                                   @Param("estado") EstadoVenta estado);

    // ==============================
    // MÉTODOS DE VERIFICACIÓN
    // ==============================

    /**
     * Verifica si una caja tiene ventas registradas.
     * Útil para evitar cerrar una caja vacía.
     */
    boolean existsByCajaIdAndEstado(Integer cajaId, EstadoVenta estado);
}
