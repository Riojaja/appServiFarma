package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReporteService {

    // ==============================
    // REPORTE DIGEMIT (RF5, RF26)
    // ==============================

    /**
     * Genera el reporte mensual para DIGEMIT.
     * Consolida la lista de productos con sus lotes, fechas de vencimiento y cantidades.
     * 
     * @param mes Año y mes del reporte (ej. 2025-06)
     * @return ReporteDigemitResponse con los datos consolidados y la fecha de generación.
     * @throws ResourceNotFoundException Si no hay datos para el período.
     */
    ReporteDigemitResponse generarReporteDigemit(String mes);

    // ==============================
    // REPORTE DE RENTABILIDAD REAL (RF11)
    // ==============================

    /**
     * Calcula la rentabilidad real del negocio, descontando mermas y costos de compra.
     * Incluye margen por producto y categoría, y el margen global.
     * 
     * @param fechaInicio Fecha de inicio del período (inclusive).
     * @param fechaFin Fecha de fin del período (inclusive).
     * @return ReporteRentabilidadResponse con el detalle de rentabilidad.
     * @throws IllegalArgumentException Si las fechas son nulas o el rango es inválido.
     */
    ReporteRentabilidadResponse generarReporteRentabilidad(LocalDate fechaInicio, LocalDate fechaFin);

    // ==============================
    // ESTADÍSTICAS DE VENTAS (RF13)
    // ==============================

    /**
     * Genera estadísticas completas de ventas para un período.
     * Incluye ventas totales, promedio diario, productos más vendidos,
     * distribución por medio de pago y tendencia diaria.
     * 
     * @param inicio Fecha y hora de inicio (inclusive).
     * @param fin Fecha y hora de fin (inclusive).
     * @return EstadisticasVentasResponse con todas las métricas.
     */
    EstadisticasVentasResponse generarEstadisticasVentas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Genera estadísticas de ventas para un período específico (día, semana o mes).
     * 
     * @param periodo Tipo de período: "dia", "semana", "mes".
     * @param fechaReferencia Fecha de referencia para el período.
     * @return EstadisticasVentasResponse con las estadísticas.
     */
    EstadisticasVentasResponse generarEstadisticasPorPeriodo(String periodo, LocalDate fechaReferencia);

    // ==============================
    // REPORTE DE PRODUCTOS MÁS VENDIDOS
    // ==============================

    /**
     * Obtiene el ranking de los productos más vendidos en un período.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @param limite Número máximo de productos a devolver.
     * @return Lista de objetos con nombre del producto, cantidad vendida y total facturado.
     */
    List<Object[]> obtenerProductosMasVendidos(LocalDateTime inicio, LocalDateTime fin, int limite);

    // ==============================
    // REPORTE DE STOCK Y VENCIMIENTOS
    // ==============================

    /**
     * Genera un reporte de productos con stock actual y alertas de vencimiento.
     * 
     * @return Lista de objetos con información de stock y vencimiento.
     */
    List<Object[]> generarReporteStockYVencimientos();
}