package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.response.MovimientoStockResponse;
import com.example.proyecto.app.entity.MovimientoStock;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoStockService {

    /**
     * Obtiene todos los movimientos de stock de un lote específico (historial completo).
     * @param loteId ID del lote.
     * @return Lista de MovimientoStockResponse ordenada por fecha descendente.
     * @throws ResourceNotFoundException Si el lote no existe.
     */
    List<MovimientoStockResponse> listarPorLote(Integer loteId);

    /**
     * Obtiene todos los movimientos de stock realizados por un usuario específico.
     * Útil para auditoría de acciones.
     * @param usuarioId ID del usuario.
     * @return Lista de MovimientoStockResponse ordenada por fecha descendente.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    List<MovimientoStockResponse> listarPorUsuario(Integer usuarioId);

    /**
     * Obtiene movimientos de stock filtrados por tipo (compra, venta, ajuste, merma).
     * @param tipoMovimiento Tipo de movimiento (no nulo).
     * @return Lista de MovimientoStockResponse ordenada por fecha descendente.
     */
    List<MovimientoStockResponse> listarPorTipo(MovimientoStock.TipoMovimiento tipoMovimiento);

    /**
     * Obtiene movimientos de stock en un rango de fechas.
     * Útil para reportes diarios, semanales o mensuales.
     * @param inicio Fecha y hora de inicio (no nula).
     * @param fin Fecha y hora de fin (no nula).
     * @return Lista de MovimientoStockResponse ordenada por fecha descendente.
     */
    List<MovimientoStockResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Lista todos los movimientos de stock registrados en el sistema.
     * Útil para auditoría general.
     * @return Lista de MovimientoStockResponse ordenada por fecha descendente.
     */
    List<MovimientoStockResponse> listarTodos();

    /**
     * Obtiene el historial de movimientos de un lote específico, filtrado por tipo.
     * @param loteId ID del lote.
     * @param tipoMovimiento Tipo de movimiento a filtrar.
     * @return Lista de MovimientoStockResponse ordenada por fecha descendente.
     * @throws ResourceNotFoundException Si el lote no existe.
     */
    List<MovimientoStockResponse> listarPorLoteYTipo(Integer loteId, MovimientoStock.TipoMovimiento tipoMovimiento);
}
