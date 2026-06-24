package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    // ==============================
    // BÚSQUEDAS POR CAMPOS BÁSICOS
    // ==============================

    /**
     * Busca un producto por su código de barras (único).
     */
    Optional<Producto> findByCodigoBarras(String codigoBarras);

    /**
     * Busca productos por nombre (coincidencia exacta, ignorando mayúsculas/minúsculas).
     */
    List<Producto> findByNombreIgnoreCase(String nombre);

    /**
     * Busca productos por nombre (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Útil para el buscador visual en ventas y gestión de inventario.
     */
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca productos por principio activo (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Útil para vincular genéricos con comerciales.
     */
    List<Producto> findByPrincipioActivoContainingIgnoreCase(String principioActivo);

    /**
     * Busca productos que sean genéricos o no (true/false).
     */
    List<Producto> findByEsGenerico(boolean esGenerico);

    // ==============================
    // BÚSQUEDAS POR RELACIONES
    // ==============================

    /**
     * Busca productos por categoría (por ID de categoría).
     */
    List<Producto> findByCategoriaId(Integer categoriaId);

    /**
     * Busca productos por fabricante (por ID de fabricante).
     */
    List<Producto> findByFabricanteId(Integer fabricanteId);

    /**
     * Busca productos asociados a un producto genérico (por ID del genérico).
     * Útil para mostrar alternativas comerciales de un genérico.
     */
    List<Producto> findByProductoGenericoId(Integer productoGenericoId);

    // ==============================
    // BÚSQUEDAS COMBINADAS
    // ==============================

    /**
     * Busca productos por nombre o código de barras (coincidencia parcial).
     * Ideal para el buscador principal de la interfaz.
     */
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoBarrasContaining(String nombre, String codigoBarras);

    /**
     * Busca productos activos que tengan stock disponible (usando una subconsulta con lotes).
     * Nota: Este método requiere una consulta personalizada porque el stock se calcula a partir de los lotes.
     * Devuelve productos que tienen al menos un lote activo con cantidad > 0.
     */
    @Query("SELECT DISTINCT p FROM Producto p JOIN p.lotes l WHERE l.estado = 'activo' AND l.cantidad > 0")
    List<Producto> findProductosConStockDisponible();

    /**
     * Busca productos con stock por debajo del stock mínimo.
     * Útil para generar alertas de reposición (requisito RF6).
     */
    @Query("SELECT p FROM Producto p WHERE p.id IN (" +
           "   SELECT l.producto.id FROM Lote l WHERE l.estado = 'activo' " +
           "   GROUP BY l.producto.id HAVING COALESCE(SUM(l.cantidad), 0) < p.stockMinimo" +
           ")")
    List<Producto> findProductosConStockBajo();

    /**
     * Busca productos sin stock disponible (ningún lote activo con cantidad > 0).
     * Útil para identificar desabastecimientos.
     */
    @Query("SELECT p FROM Producto p WHERE NOT EXISTS (" +
           "   SELECT l FROM Lote l WHERE l.producto.id = p.id AND l.estado = 'activo' AND l.cantidad > 0" +
           ")")
    List<Producto> findProductosSinStock();

    // ==============================
    // CONSULTAS DE AGREGACIÓN
    // ==============================

    /**
     * Cuenta cuántos productos tiene una categoría.
     */
    long countByCategoriaId(Integer categoriaId);

    /**
     * Cuenta cuántos productos tiene un fabricante.
     */
    long countByFabricanteId(Integer fabricanteId);

    /**
     * Cuenta cuántos productos son genéricos.
     */
    long countByEsGenerico(boolean esGenerico);

    // ==============================
    // CONSULTAS PARA REPORTES
    // ==============================

    /**
     * Obtiene todos los productos ordenados por nombre (A-Z).
     */
    List<Producto> findAllByOrderByNombreAsc();

    /**
     * Obtiene los productos más vendidos (según cantidad en detalle_ventas).
     * Útil para reportes de tendencias y estadísticas.
     */
    @Query("SELECT p, SUM(dv.cantidad) as totalVendido FROM Producto p " +
           "JOIN p.lotes l JOIN l.detalleVentas dv " +
           "GROUP BY p.id ORDER BY totalVendido DESC")
    List<Object[]> findProductosMasVendidos();

    /**
     * Obtiene el precio promedio de venta de un producto (basado en sus lotes activos).
     */
    @Query("SELECT COALESCE(AVG(l.precioVenta), 0) FROM Lote l WHERE l.producto.id = :productoId AND l.estado = 'activo'")
    BigDecimal avgPrecioVentaByProductoId(@Param("productoId") Integer productoId);
    
    /**
     * Verifica si existe un producto con un código de barras específico.
     * Útil para validar duplicados al crear o actualizar.
     */
    boolean existsByCodigoBarras(String codigoBarras);

    /**
     * Busca un producto por su código de barras (exacto).
     * Esencial para el lector de códigos de barras en ventas.
     */
    

    /**
     * (Opcional) Productos con stock bajo (consulta personalizada).
     * Útil para alertas de reposición.
     */
   
   

}