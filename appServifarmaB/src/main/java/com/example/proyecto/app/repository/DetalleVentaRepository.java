package com.example.proyecto.app.repository;

import com.example.proyecto.app.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {

    /**
     * Obtiene todos los detalles de una venta específica (por ID de venta).
     * Útil para mostrar el detalle de una boleta o factura.
     */
    List<DetalleVenta> findByVentaId(Integer ventaId);

    /**
     * Obtiene todos los detalles de una venta, ordenados por ID de detalle (orden de ingreso).
     */
    List<DetalleVenta> findByVentaIdOrderByIdAsc(Integer ventaId);

    /**
     * Obtiene todos los detalles que corresponden a un lote específico.
     * Útil para saber en qué ventas se ha usado un lote (trazabilidad).
     */
    List<DetalleVenta> findByLoteId(Integer loteId);

    /**
     * Obtiene el subtotal total de una venta específica (suma de todos los subtotales de sus detalles).
     * Nota: El subtotal ya está calculado en la BD (columna GENERATED ALWAYS).
     * Este método puede ser usado para verificar el total de la venta.
     */
    @Query("SELECT COALESCE(SUM(d.subtotal), 0) FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    BigDecimal sumSubtotalByVentaId(@Param("ventaId") Integer ventaId);

    /**
     * Obtiene el costo total de compra de una venta (suma de cantidad * precio_compra_unitario).
     * Útil para calcular el margen de ganancia real (rentabilidad).
     */
    @Query("SELECT COALESCE(SUM(d.cantidad * d.precioCompraUnitario), 0) FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    BigDecimal sumCostoCompraByVentaId(@Param("ventaId") Integer ventaId);

    /**
     * Obtiene la cantidad total de productos vendidos en una venta (suma de cantidades).
     */
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    Integer sumCantidadByVentaId(@Param("ventaId") Integer ventaId);

    /**
     * Cuenta cuántas unidades de un producto (identificado por lote) se han vendido en total.
     * Útil para estadísticas de rotación por lote.
     */
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleVenta d WHERE d.lote.id = :loteId")
    Integer sumCantidadByLoteId(@Param("loteId") Integer loteId);
    
    
    @Query("SELECT COALESCE(SUM(d.cantidad * d.precioCompraUnitario), 0) FROM DetalleVenta d WHERE d.createdAt BETWEEN :inicio AND :fin")
    BigDecimal sumCostoCompraByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT c.nombre, SUM(d.cantidad * d.precioUnitarioVenta), SUM(d.cantidad * d.precioCompraUnitario) " +
           "FROM DetalleVenta d JOIN d.lote l JOIN l.producto p JOIN p.categoria c " +
           "WHERE d.createdAt BETWEEN :inicio AND :fin " +
           "GROUP BY c.nombre")
    List<Object[]> findRentabilidadPorCategoria(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT p.id, p.nombre, SUM(d.cantidad), SUM(d.cantidad * d.precioUnitarioVenta) " +
           "FROM DetalleVenta d JOIN d.lote l JOIN l.producto p " +
           "WHERE d.createdAt BETWEEN :inicio AND :fin " +
           "GROUP BY p.id, p.nombre " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> findTopProductosVendidos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                                            @Param("limite") int limite);
    
}