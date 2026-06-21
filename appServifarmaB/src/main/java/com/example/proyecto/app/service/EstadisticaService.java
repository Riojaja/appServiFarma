package com.example.proyecto.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EstadisticaService {

    // ==============================
    // RESUMEN PARA DASHBOARD
    // ==============================

    /**
     * Obtiene un resumen del día actual (ventas totales, número de transacciones, ticket promedio).
     * 
     * @return Map con las métricas del día.
     */
    Map<String, Object> obtenerResumenDiario();

    /**
     * Obtiene un resumen de ventas para una fecha específica.
     * 
     * @param fecha Fecha a consultar.
     * @return Map con las métricas del día.
     */
    Map<String, Object> obtenerResumenDiario(LocalDate fecha);

    /**
     * Obtiene un resumen de la semana actual (lunes a domingo).
     * 
     * @return Map con las métricas de la semana.
     */
    Map<String, Object> obtenerResumenSemanal();

    /**
     * Obtiene un resumen del mes actual.
     * 
     * @return Map con las métricas del mes.
     */
    Map<String, Object> obtenerResumenMensual();

    /**
     * Obtiene un resumen de un mes específico.
     * 
     * @param anio Año.
     * @param mes  Mes (1-12).
     * @return Map con las métricas del mes.
     */
    Map<String, Object> obtenerResumenMensual(int anio, int mes);

    // ==============================
    // DISTRIBUCIÓN POR MEDIOS DE PAGO
    // ==============================

    /**
     * Obtiene la distribución de ventas por medio de pago en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin    Fecha y hora de fin.
     * @return Lista de Object[] donde cada elemento contiene [medioPago, totalVendido, cantidadTransacciones].
     */
    List<Object[]> obtenerDistribucionMediosPago(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene la distribución de ventas por medio de pago del día actual.
     * 
     * @return Lista de Object[] con la distribución del día.
     */
    List<Object[]> obtenerDistribucionMediosPagoDiario();

    // ==============================
    // TENDENCIAS POR HORA Y DÍA
    // ==============================

    /**
     * Obtiene las ventas agrupadas por hora para una fecha específica.
     * Útil para gráficos de tendencia horaria.
     * 
     * @param fecha Fecha a consultar.
     * @return Lista de Object[] donde cada elemento contiene [hora, totalVendido].
     */
    List<Object[]> obtenerVentasPorHora(LocalDate fecha);

    /**
     * Obtiene las ventas agrupadas por día en un rango de fechas.
     * Útil para gráficos de tendencia diaria.
     * 
     * @param inicio Fecha de inicio.
     * @param fin    Fecha de fin.
     * @return Lista de Object[] donde cada elemento contiene [fecha, totalVendido].
     */
    List<Object[]> obtenerVentasPorDia(LocalDate inicio, LocalDate fin);

    // ==============================
    // INDICADORES CLAVE (KPI)
    // ==============================

    /**
     * Obtiene el ticket promedio en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin    Fecha y hora de fin.
     * @return Ticket promedio como BigDecimal.
     */
    BigDecimal obtenerTicketPromedio(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene el total de ventas de un período específico.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin    Fecha y hora de fin.
     * @return Total de ventas.
     */
    BigDecimal obtenerTotalVentas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene el número total de transacciones en un período.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin    Fecha y hora de fin.
     * @return Número de transacciones.
     */
    Long obtenerTotalTransacciones(LocalDateTime inicio, LocalDateTime fin);
}