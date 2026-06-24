package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.repository.VentaRepository;
import com.example.proyecto.app.service.EstadisticaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EstadisticaServiceImpl implements EstadisticaService {

    private final VentaRepository ventaRepository;

    // ==============================
    // RESÚMENES PARA DASHBOARD
    // ==============================

    @Override
    public Map<String, Object> obtenerResumenDiario() {
        return obtenerResumenDiario(LocalDate.now());
    }

    @Override
    public Map<String, Object> obtenerResumenDiario(LocalDate fecha) {
        log.debug("Obteniendo resumen diario para: {}", fecha);

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        return buildResumen(inicio, fin);
    }

    @Override
    public Map<String, Object> obtenerResumenSemanal() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate finSemana = hoy.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        log.debug("Obteniendo resumen semanal desde {} hasta {}", inicioSemana, finSemana);

        LocalDateTime inicio = inicioSemana.atStartOfDay();
        LocalDateTime fin = finSemana.atTime(LocalTime.MAX);

        Map<String, Object> resumen = buildResumen(inicio, fin);
        resumen.put("inicioSemana", inicioSemana);
        resumen.put("finSemana", finSemana);
        return resumen;
    }

    @Override
    public Map<String, Object> obtenerResumenMensual() {
        LocalDate hoy = LocalDate.now();
        return obtenerResumenMensual(hoy.getYear(), hoy.getMonthValue());
    }

    @Override
    public Map<String, Object> obtenerResumenMensual(int anio, int mes) {
        LocalDate inicioMes = LocalDate.of(anio, mes, 1);
        LocalDate finMes = inicioMes.with(TemporalAdjusters.lastDayOfMonth());

        log.debug("Obteniendo resumen mensual para {}/{}", mes, anio);

        LocalDateTime inicio = inicioMes.atStartOfDay();
        LocalDateTime fin = finMes.atTime(LocalTime.MAX);

        Map<String, Object> resumen = buildResumen(inicio, fin);
        resumen.put("mes", mes);
        resumen.put("anio", anio);
        return resumen;
    }

    /**
     * Construye el mapa de resumen para un período dado.
     */
    private Map<String, Object> buildResumen(LocalDateTime inicio, LocalDateTime fin) {
        BigDecimal totalVentas = ventaRepository.sumTotalByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.completada);
        Long totalTransacciones = ventaRepository.countByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.completada);

        BigDecimal ticketPromedio = BigDecimal.ZERO;
        if (totalTransacciones > 0) {
            ticketPromedio = totalVentas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP);
        }

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("totalVentas", totalVentas);
        resumen.put("totalTransacciones", totalTransacciones);
        resumen.put("ticketPromedio", ticketPromedio);
        resumen.put("fechaInicio", inicio);
        resumen.put("fechaFin", fin);

        return resumen;
    }

    // ==============================
    // DISTRIBUCIÓN POR MEDIOS DE PAGO
    // ==============================

    @Override
    public List<Object[]> obtenerDistribucionMediosPago(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo distribución de medios de pago entre {} y {}", inicio, fin);
        return ventaRepository.findTotalVentasAgrupadoPorMedioPago(inicio, fin, Venta.EstadoVenta.completada);
    }

    @Override
    public List<Object[]> obtenerDistribucionMediosPagoDiario() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);
        return obtenerDistribucionMediosPago(inicio, fin);
    }

    // ==============================
    // TENDENCIAS POR HORA Y DÍA
    // ==============================

    @Override
    public List<Object[]> obtenerVentasPorHora(LocalDate fecha) {
        log.debug("Obteniendo ventas por hora para: {}", fecha);

        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        // Nota: Este método debe existir en VentaRepository.
        // Agrupamos por HOUR(fecha) usando la función nativa de MySQL.
        // En JPQL usamos FUNCTION('HOUR', v.fecha)
        return ventaRepository.findTotalVentasAgrupadoPorHora(inicio, fin, Venta.EstadoVenta.completada);
    }

    @Override
    public List<Object[]> obtenerVentasPorDia(LocalDate inicio, LocalDate fin) {
        log.debug("Obteniendo ventas por día desde {} hasta {}", inicio, fin);

        LocalDateTime inicioDateTime = inicio.atStartOfDay();
        LocalDateTime finDateTime = fin.atTime(LocalTime.MAX);

        return ventaRepository.findTotalVentasAgrupadoPorDia(inicioDateTime, finDateTime, Venta.EstadoVenta.completada);
    }

    // ==============================
    // INDICADORES CLAVE (KPI)
    // ==============================

    @Override
    public BigDecimal obtenerTicketPromedio(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Calculando ticket promedio entre {} y {}", inicio, fin);

        BigDecimal totalVentas = ventaRepository.sumTotalByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.completada);
        Long totalTransacciones = ventaRepository.countByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.completada);

        if (totalTransacciones == 0) {
            return BigDecimal.ZERO;
        }
        return totalVentas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal obtenerTotalVentas(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Calculando total de ventas entre {} y {}", inicio, fin);
        return ventaRepository.sumTotalByFechaBetweenAndEstado(inicio, fin, Venta.EstadoVenta.completada);
    }

    @Override
    public Long obtenerTotalTransacciones(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Contando transacciones entre {} y {}", inicio, fin);
        return ventaRepository.countByFechaBetweenAndEstado(inicio, fin, Venta.EstadoVenta.completada);
    }
}