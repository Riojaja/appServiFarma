package com.example.proyecto.app.service;

import com.example.proyecto.app.entity.Lote;

import java.util.List;

public interface InventarioService {

    // ==============================
    // CÁLCULO DE STOCK
    // ==============================

    /**
     * Calcula el stock total de un producto sumando las cantidades de todos sus lotes activos.
     *
     * @param productoId ID del producto
     * @return Cantidad total disponible en stock (0 si no hay lotes activos)
     * @throws ResourceNotFoundException Si el producto no existe
     */
    Integer calcularStockProducto(Integer productoId);

    // ==============================
    // LÓGICA FEFO (First Expired, First Out)
    // ==============================

    /**
     * Obtiene el lote activo más próximo a vencer para un producto (lógica FEFO).
     * Este es el lote que debe ser despachado primero en una venta.
     *
     * @param productoId ID del producto
     * @return Lote con la fecha de vencimiento más próxima y stock disponible (> 0)
     * @throws ResourceNotFoundException Si el producto no existe
     * @throws StockInsuficienteException Si no hay lotes activos con stock disponible
     */
    Lote obtenerLoteFEFO(Integer productoId);

    // ==============================
    // DESCUENTO DE STOCK
    // ==============================

    /**
     * Descuenta una cantidad específica de un lote y registra el movimiento de stock.
     * Este método es llamado automáticamente al registrar una venta.
     *
     * @param loteId      ID del lote a descontar
     * @param cantidad    Cantidad a descontar (debe ser > 0)
     * @param usuarioId   ID del usuario que realiza la operación (para auditoría)
     * @param referenciaId ID de referencia (ej. ID de la venta)
     * @throws ResourceNotFoundException Si el lote no existe
     * @throws StockInsuficienteException Si la cantidad a descontar excede el stock disponible
     * @throws LoteVencidoException Si el lote ya está vencido
     * @throws MovimientoInvalidoException Si la cantidad es <= 0
     */
    void descontarStock(Integer loteId, Integer cantidad, Integer usuarioId, Integer referenciaId);

    // ==============================
    // ACTUALIZACIÓN DE ESTADOS DE LOTES (Tareas programadas)
    // ==============================

    /**
     * Actualiza el estado de todos los lotes que han caducado (fecha de vencimiento anterior a hoy).
     * Cambia el estado de 'activo' a 'vencido'.
     * Este método está diseñado para ser ejecutado por una tarea programada (Scheduler).
     *
     * @return Número de lotes actualizados a estado 'vencido'
     */
    int actualizarLotesVencidos();

    /**
     * Actualiza el estado de los lotes a 'agotado' cuando su cantidad llega a 0.
     * Este método se ejecuta automáticamente después de descontar stock.
     *
     * @return Número de lotes actualizados a estado 'agotado'
     */
    int actualizarLotesAgotados();

    // ==============================
    // GENERACIÓN DE ALERTAS (usando ParametroSistema)
    // ==============================

    /**
     * Obtiene los IDs de los productos cuyo stock total está por debajo del umbral mínimo.
     * El umbral se obtiene del parámetro del sistema 'stock_minimo_global' (o del stock_minimo de cada producto).
     *
     * @return Lista de IDs de productos con stock bajo
     */
    List<Integer> obtenerProductosConStockBajo();

    /**
     * Obtiene los lotes activos que están próximos a vencer, según los días de anticipación
     * configurados en el parámetro del sistema 'dias_alerta_vencimiento'.
     *
     * @return Lista de lotes próximos a vencer (con días restantes <= días de anticipación)
     */
    List<Lote> obtenerLotesProximosAVencer();

    /**
     * Obtiene todos los lotes activos que tienen stock disponible (cantidad > 0).
     * Útil para procesos de verificación y reportes.
     *
     * @return Lista de lotes activos con stock
     */
    List<Lote> obtenerLotesActivosConStock();
}
