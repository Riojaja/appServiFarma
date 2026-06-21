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

    List<Venta> findByUsuarioId(Integer usuarioId);
    List<Venta> findByClienteId(Integer clienteId);
    List<Venta> findByCajaId(Integer cajaId);

    // ==============================
    // FILTRADO POR ESTADO
    // ==============================

    List<Venta> findByEstado(EstadoVenta estado);
    List<Venta> findByCajaIdAndEstado(Integer cajaId, EstadoVenta estado);

    // ==============================
    // FILTRADO POR MEDIO DE PAGO
    // ==============================

    List<Venta> findByMedioPago(MedioPago medioPago);
    List<Venta> findByMedioPagoAndFechaBetween(MedioPago medioPago, LocalDateTime inicio, LocalDateTime fin);

    // ==============================
    // FILTRADO POR FECHAS (REPORTES)
    // ==============================

    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByFechaBetweenAndEstado(LocalDateTime inicio, LocalDateTime fin, EstadoVenta estado);

    // ==============================
    // CONSULTAS DE AGREGACIÓN (SUM, COUNT, AVG)
    // ==============================

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.caja.id = :cajaId AND v.estado = :estado")
    BigDecimal sumTotalByCajaIdAndEstado(@Param("cajaId") Integer cajaId, @Param("estado") EstadoVenta estado);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado")
    BigDecimal sumTotalByFechaBetweenAndEstado(@Param("inicio") LocalDateTime inicio,
                                               @Param("fin") LocalDateTime fin,
                                               @Param("estado") EstadoVenta estado);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.cliente.id = :clienteId AND v.estado = :estado")
    BigDecimal sumTotalByClienteIdAndEstado(@Param("clienteId") Integer clienteId, @Param("estado") EstadoVenta estado);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.medioPago = :medioPago AND v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado")
    BigDecimal sumTotalByMedioPagoAndFechaBetween(@Param("medioPago") MedioPago medioPago,
                                                   @Param("inicio") LocalDateTime inicio,
                                                   @Param("fin") LocalDateTime fin,
                                                   @Param("estado") EstadoVenta estado);

    long countByMedioPagoAndFechaBetweenAndEstado(MedioPago medioPago, LocalDateTime inicio, LocalDateTime fin, EstadoVenta estado);

    // ==============================
    // CONSULTAS PARA REPORTES Y ESTADÍSTICAS
    // ==============================

    @Query("SELECT v FROM Venta v WHERE v.estado = :estado ORDER BY v.fecha DESC")
    List<Venta> findTopVentasRecientes(@Param("estado") EstadoVenta estado);

    @Query("SELECT v FROM Venta v JOIN FETCH v.detalles d JOIN FETCH d.lote l JOIN FETCH l.producto p WHERE v.id = :id")
    Optional<Venta> findByIdWithDetalles(@Param("id") Integer id);

    @Query("SELECT v FROM Venta v JOIN FETCH v.detalles d JOIN FETCH d.lote l JOIN FETCH l.producto p WHERE v.cliente.id = :clienteId AND v.estado = :estado")
    List<Venta> findByClienteIdWithDetalles(@Param("clienteId") Integer clienteId, @Param("estado") EstadoVenta estado);

    @Query("SELECT DATE(v.fecha) as fecha, SUM(v.total) as total FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado " +
           "GROUP BY DATE(v.fecha) ORDER BY fecha ASC")
    List<Object[]> findTotalVentasAgrupadoPorDia(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin,
                                                 @Param("estado") EstadoVenta estado);

    @Query("SELECT YEAR(v.fecha) as anio, MONTH(v.fecha) as mes, SUM(v.total) as total FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado " +
           "GROUP BY YEAR(v.fecha), MONTH(v.fecha) ORDER BY anio ASC, mes ASC")
    List<Object[]> findTotalVentasAgrupadoPorMes(@Param("inicio") LocalDateTime inicio,
                                                 @Param("fin") LocalDateTime fin,
                                                 @Param("estado") EstadoVenta estado);

    // ==============================
    // MÉTODOS PARA ESTADÍSTICAS (Fase 5)
    // ==============================

    /**
     * Cuenta el número de ventas completadas en un rango de fechas.
     */
    long countByFechaBetweenAndEstado(LocalDateTime inicio, LocalDateTime fin, EstadoVenta estado);

    /**
     * Obtiene el total de ventas agrupado por medio de pago en un rango de fechas.
     * Retorna una lista de Object[] donde cada elemento es [medioPago, total, cantidadTransacciones].
     */
    @Query("SELECT v.medioPago, SUM(v.total), COUNT(v.id) FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado " +
           "GROUP BY v.medioPago")
    List<Object[]> findTotalVentasAgrupadoPorMedioPago(@Param("inicio") LocalDateTime inicio,
                                                       @Param("fin") LocalDateTime fin,
                                                       @Param("estado") EstadoVenta estado);

    /**
     * Obtiene el total de ventas agrupado por hora en un rango de fechas.
     * Retorna una lista de Object[] donde cada elemento es [hora, total].
     */
    @Query("SELECT FUNCTION('HOUR', v.fecha) as hora, SUM(v.total) FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin AND v.estado = :estado " +
           "GROUP BY FUNCTION('HOUR', v.fecha) ORDER BY hora ASC")
    List<Object[]> findTotalVentasAgrupadoPorHora(@Param("inicio") LocalDateTime inicio,
                                                  @Param("fin") LocalDateTime fin,
                                                  @Param("estado") EstadoVenta estado);

    // ==============================
    // MÉTODOS DE VERIFICACIÓN
    // ==============================

    boolean existsByCajaIdAndEstado(Integer cajaId, EstadoVenta estado);
    
}