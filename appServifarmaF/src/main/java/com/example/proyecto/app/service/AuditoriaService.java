package com.example.proyecto.app.service;

import com.example.proyecto.app.entity.MovimientoStock;
import com.example.proyecto.app.entity.Venta;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditoriaService {

    // ==============================
    // AUDITORÍA DE MOVIMIENTOS DE STOCK
    // ==============================

    /**
     * Obtiene todos los movimientos de stock realizados por un usuario en un rango de fechas.
     * 
     * @param usuarioId ID del usuario.
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Lista de MovimientoStock.
     */
    List<MovimientoStock> obtenerMovimientosPorUsuario(Integer usuarioId, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene todos los movimientos de stock de un tipo específico en un rango de fechas.
     * 
     * @param tipo Tipo de movimiento (compra, venta, ajuste, merma).
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Lista de MovimientoStock.
     */
    List<MovimientoStock> obtenerMovimientosPorTipo(MovimientoStock.TipoMovimiento tipo, LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene el historial completo de movimientos de un lote específico.
     * 
     * @param loteId ID del lote.
     * @return Lista de MovimientoStock ordenada por fecha descendente.
     */
    List<MovimientoStock> obtenerHistorialLote(Integer loteId);

    // ==============================
    // AUDITORÍA DE VENTAS
    // ==============================

    /**
     * Obtiene todas las ventas anuladas en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Lista de Venta anuladas.
     */
    List<Venta> obtenerVentasAnuladas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene todas las ventas realizadas por un usuario en un rango de fechas.
     * 
     * @param usuarioId ID del usuario.
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Lista de Venta.
     */
    List<Venta> obtenerVentasPorUsuario(Integer usuarioId, LocalDateTime inicio, LocalDateTime fin);

    // ==============================
    // AUDITORÍA DE CAJA
    // ==============================

    /**
     * Obtiene todas las aperturas y cierres de caja realizados por un usuario.
     * 
     * @param usuarioId ID del usuario.
     * @return Lista de Map con datos de apertura/cierre.
     */
    List<Map<String, Object>> obtenerHistorialCajaPorUsuario(Integer usuarioId);

    /**
     * Obtiene las cajas cerradas con diferencia (sobrante/faltante) en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Lista de Map con datos de cierre.
     */
    List<Map<String, Object>> obtenerCajasConDiferencia(LocalDateTime inicio, LocalDateTime fin);

    // ==============================
    // AUDITORÍA GENERAL
    // ==============================

    /**
     * Obtiene un resumen de actividad del sistema en un rango de fechas:
     * - Total de ventas
     * - Total de movimientos de stock
     * - Total de anulaciones
     * - Total de aperturas/cierres de caja
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Map con las métricas de actividad.
     */
    Map<String, Object> obtenerResumenActividad(LocalDateTime inicio, LocalDateTime fin);
}