package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.response.EstadisticasVentasResponse;
import com.example.proyecto.app.dto.response.ReporteDigemitResponse;
import com.example.proyecto.app.dto.response.ReporteRentabilidadResponse;
import com.example.proyecto.app.entity.*;
import com.example.proyecto.app.repository.*;
import com.example.proyecto.app.service.ReporteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    

    // ==============================
    // REPORTE DIGEMIT (RF5, RF26)
    // ==============================

    @Override
    public ReporteDigemitResponse generarReporteDigemit(String mes) {
        log.info("Generando reporte DIGEMIT para el mes: {}", mes);

        // Parsear el mes (formato: YYYY-MM)
        String[] parts = mes.split("-");
        int year = Integer.parseInt(parts[0]);
        int month = Integer.parseInt(parts[1]);

        // Obtener todas las ventas del mes (para filtrar productos que tuvieron movimiento)
        // O mejor: Obtener todos los productos y sus lotes activos/vencidos para el mes.
        // Según la normativa, DIGEMIT pide el listado de todos los productos con sus lotes y vencimientos.
        List<Producto> productos = productoRepository.findAll();

        List<ReporteDigemitResponse.Item> items = new ArrayList<>();

        for (Producto producto : productos) {
            // Obtener todos los lotes del producto (activos, vencidos, agotados, etc.)
        	List<Lote> lotes = loteRepository.findByProductoId(producto.getId());

            for (Lote lote : lotes) {
                // Solo incluir lotes que existieron/estuvieron activos en el mes (fecha ingreso <= fin de mes)
                LocalDate inicioMes = LocalDate.of(year, month, 1);
                LocalDate finMes = inicioMes.with(TemporalAdjusters.lastDayOfMonth());

                if (lote.getFechaIngreso().isBefore(finMes.plusDays(1))) {
                    items.add(ReporteDigemitResponse.Item.builder()
                            .codigoProducto(producto.getCodigoBarras() != null ? producto.getCodigoBarras() : "N/A")
                            .nombreProducto(producto.getNombre())
                            .laboratorio(producto.getFabricante() != null ? producto.getFabricante().getNombre() : "N/A")
                            .principioActivo(producto.getPrincipioActivo() != null ? producto.getPrincipioActivo() : "N/A")
                            .lote(lote.getLote())
                            .fechaVencimiento(lote.getFechaVencimiento())
                            .cantidad(lote.getCantidad() > 0 ? lote.getCantidad() : 0) // Stock actual del lote
                            .build());
                }
            }
        }

        log.info("Reporte DIGEMIT generado con {} items", items.size());
        return ReporteDigemitResponse.builder()
                .mes(mes)
                .fechaGeneracion(LocalDate.now())
                .items(items)
                .build();
    }

    // ==============================
    // REPORTE DE RENTABILIDAD REAL (RF11)
    // ==============================

    @Override
    public ReporteRentabilidadResponse generarReporteRentabilidad(LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte de rentabilidad desde {} hasta {}", fechaInicio, fechaFin);

        // 1. Calcular ingresos totales (ventas completadas en el período)
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);

        BigDecimal ingresosTotales = ventaRepository.sumTotalByFechaBetweenAndEstado(
                inicioDateTime, finDateTime, Venta.EstadoVenta.completada);

        // 2. Calcular costo de ventas (suma de precio_compra_unitario * cantidad en detalle_ventas)
        BigDecimal costoVentas = detalleVentaRepository.sumCostoCompraByFechaBetween(inicioDateTime, finDateTime);

        // 3. Calcular mermas (productos caducados o deteriorados en el período)
        BigDecimal mermas = calcularMermas(fechaInicio, fechaFin);

        // 4. Calcular margen bruto y neto
        BigDecimal margenBruto = ingresosTotales.subtract(costoVentas);
        BigDecimal margenNeto = margenBruto.subtract(mermas);

        // 5. Calcular rentabilidad por categoría (opcional)
        List<ReporteRentabilidadResponse.RentabilidadCategoria> categorias = calcularRentabilidadPorCategoria(
                inicioDateTime, finDateTime);

        log.info("Reporte de rentabilidad generado: Ingresos={}, Costo={}, Mermas={}, MargenNeto={}",
                ingresosTotales, costoVentas, mermas, margenNeto);

        return ReporteRentabilidadResponse.builder()
                .fechaInicio(fechaInicio)
                .fechaFin(fechaFin)
                .ingresosTotales(ingresosTotales)
                .costoVentas(costoVentas)
                .mermas(mermas)
                .margenBruto(margenBruto)
                .margenNeto(margenNeto)
                .categorias(categorias)
                .build();
    }

    /**
     * Calcula el total de mermas (pérdidas por caducidad y deterioro) en el período.
     */
    private BigDecimal calcularMermas(LocalDate fechaInicio, LocalDate fechaFin) {
        BigDecimal totalMermas = BigDecimal.ZERO;

        // 1. Mermas registradas en movimientos_stock (tipo 'merma')
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(23, 59, 59);

        // Obtener sumatoria de mermas de movimientos_stock
        // Nota: Necesitamos un método en MovimientoStockRepository para sumar por tipo y fecha
        // Si no existe, podemos implementar una consulta directa o usar un método existente.
        // Usaremos un método asumido: sumCantidadByTipoMovimientoAndFechaBetween
        // (debe devolver el costo unitario * cantidad de las mermas)
        // Como no lo tenemos definido, lo calculamos manualmente.
        List<MovimientoStock> movimientosMerma = movimientoStockRepository
                .findByTipoMovimientoAndFechaBetween(MovimientoStock.TipoMovimiento.merma, inicioDateTime, finDateTime);

        for (MovimientoStock m : movimientosMerma) {
            if (m.getCostoUnitario() != null) {
                totalMermas = totalMermas.add(m.getCostoUnitario().multiply(BigDecimal.valueOf(m.getCantidad())));
            } else {
                // Si no tiene costo unitario, usamos el precio de compra del lote
                Lote lote = m.getLote();
                if (lote != null) {
                    totalMermas = totalMermas.add(lote.getPrecioCompra().multiply(BigDecimal.valueOf(m.getCantidad())));
                }
            }
        }

        // 2. Productos que caducaron en el período (lotes marcados como vencidos)
        List<Lote> lotesVencidos = loteRepository.findByFechaVencimientoBetweenAndEstado(
                fechaInicio, fechaFin, Lote.EstadoLote.vencido);

        for (Lote lote : lotesVencidos) {
            totalMermas = totalMermas.add(lote.getPrecioCompra().multiply(BigDecimal.valueOf(lote.getCantidad())));
        }

        return totalMermas;
    }

    /**
     * Calcula la rentabilidad desglosada por categoría.
     */
    private List<ReporteRentabilidadResponse.RentabilidadCategoria> calcularRentabilidadPorCategoria(
            LocalDateTime inicio, LocalDateTime fin) {

        // Obtener todos los detalles de venta con su producto y categoría
        // Nota: Este método debe existir en DetalleVentaRepository.
        // Simulamos la lógica con una consulta.
        List<Object[]> resultados = detalleVentaRepository.findRentabilidadPorCategoria(inicio, fin);

        Map<String, ReporteRentabilidadResponse.RentabilidadCategoria> mapa = new HashMap<>();

        for (Object[] row : resultados) {
            String categoriaNombre = (String) row[0];
            BigDecimal ingresos = (BigDecimal) row[1];
            BigDecimal costos = (BigDecimal) row[2];

            ReporteRentabilidadResponse.RentabilidadCategoria cat = mapa.getOrDefault(categoriaNombre,
                    ReporteRentabilidadResponse.RentabilidadCategoria.builder()
                            .categoriaNombre(categoriaNombre)
                            .ingresos(BigDecimal.ZERO)
                            .costos(BigDecimal.ZERO)
                            .build());

            cat.setIngresos(cat.getIngresos().add(ingresos));
            cat.setCostos(cat.getCostos().add(costos));
            cat.setMargen(cat.getIngresos().subtract(cat.getCostos()));
            mapa.put(categoriaNombre, cat);
        }

        return new ArrayList<>(mapa.values());
    }

    // ==============================
    // ESTADÍSTICAS DE VENTAS (RF13)
    // ==============================

    @Override
    public EstadisticasVentasResponse generarEstadisticasVentas(LocalDateTime inicio, LocalDateTime fin) {
        log.info("Generando estadísticas de ventas desde {} hasta {}", inicio, fin);

        // 1. Resumen general
        BigDecimal totalVentas = ventaRepository.sumTotalByFechaBetweenAndEstado(inicio, fin, Venta.EstadoVenta.completada);
        Long totalTransacciones = ventaRepository.countByFechaBetweenAndEstado(inicio, fin, Venta.EstadoVenta.completada);
        BigDecimal ticketPromedio = totalTransacciones > 0 ?
                totalVentas.divide(BigDecimal.valueOf(totalTransacciones), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        // 2. Distribución por medio de pago
        List<Object[]> distribucionPagos = ventaRepository.findTotalVentasAgrupadoPorMedioPago(inicio, fin, Venta.EstadoVenta.completada);

        // 3. Productos más vendidos
        List<Object[]> topProductos = detalleVentaRepository.findTopProductosVendidos(inicio, fin, 10);

        // 4. Tendencia diaria
        List<Object[]> tendenciaDiaria = ventaRepository.findTotalVentasAgrupadoPorDia(
                inicio, fin, Venta.EstadoVenta.completada);

        // 5. Comparativa con período anterior (opcional)
        // Calcular período anterior (misma duración)
        LocalDateTime inicioAnterior = inicio.minusDays(inicio.until(fin, java.time.temporal.ChronoUnit.DAYS) + 1);
        LocalDateTime finAnterior = inicio.minusSeconds(1);
        BigDecimal totalAnterior = ventaRepository.sumTotalByFechaBetweenAndEstado(
                inicioAnterior, finAnterior, Venta.EstadoVenta.completada);
        BigDecimal variacion = totalAnterior.compareTo(BigDecimal.ZERO) != 0 ?
                totalVentas.subtract(totalAnterior).divide(totalAnterior, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) :
                BigDecimal.ZERO;

        return EstadisticasVentasResponse.builder()
                .fechaInicio(inicio)
                .fechaFin(fin)
                .totalVentas(totalVentas)
                .totalTransacciones(totalTransacciones)
                .ticketPromedio(ticketPromedio)
                .distribucionMediosPago(convertirDistribucion(distribucionPagos))
                .productosMasVendidos(convertirTopProductos(topProductos))
                .tendenciaDiaria(convertirTendencia(tendenciaDiaria))
                .variacionPorcentual(variacion)
                .build();
    }

    @Override
    public EstadisticasVentasResponse generarEstadisticasPorPeriodo(String periodo, LocalDate fechaReferencia) {
        LocalDateTime inicio, fin;

        switch (periodo.toLowerCase()) {
            case "dia":
                inicio = fechaReferencia.atStartOfDay();
                fin = fechaReferencia.atTime(23, 59, 59);
                break;
            case "semana":
                inicio = fechaReferencia.with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).atStartOfDay();
                fin = fechaReferencia.with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).atTime(23, 59, 59);
                break;
            case "mes":
                inicio = fechaReferencia.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
                fin = fechaReferencia.with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);
                break;
            default:
                throw new IllegalArgumentException("Período inválido. Use 'dia', 'semana' o 'mes'.");
        }

        return generarEstadisticasVentas(inicio, fin);
    }

    // ==============================
    // PRODUCTOS MÁS VENDIDOS
    // ==============================

    @Override
    public List<Object[]> obtenerProductosMasVendidos(LocalDateTime inicio, LocalDateTime fin, int limite) {
        log.info("Obteniendo top {} productos más vendidos entre {} y {}", limite, inicio, fin);
        return detalleVentaRepository.findTopProductosVendidos(inicio, fin, limite);
    }

    // ==============================
    // REPORTE DE STOCK Y VENCIMIENTOS
    // ==============================

    @Override
    public List<Object[]> generarReporteStockYVencimientos() {
        log.info("Generando reporte de stock y vencimientos");

        List<Object[]> resultados = new ArrayList<>();

        // Obtener todos los productos
        List<Producto> productos = productoRepository.findAll();

        for (Producto producto : productos) {
            // Calcular stock total
            Integer stockTotal = loteRepository.sumCantidadByProductoIdAndEstado(
                    producto.getId(), Lote.EstadoLote.activo);

            // Obtener lotes próximos a vencer (30 días)
            LocalDate hoy = LocalDate.now();
            LocalDate limite = hoy.plusDays(30);
            List<Lote> lotesProximos = loteRepository.findByFechaVencimientoBetweenAndEstado(
                    hoy, limite, Lote.EstadoLote.activo);

            String alerta = "OK";
            if (stockTotal == 0) {
                alerta = "SIN STOCK";
            } else if (stockTotal < producto.getStockMinimo()) {
                alerta = "STOCK BAJO";
            } else if (!lotesProximos.isEmpty()) {
                alerta = "PRÓXIMO A VENCER (" + lotesProximos.size() + " lotes)";
            }

            resultados.add(new Object[]{
                    producto.getId(),
                    producto.getNombre(),
                    stockTotal,
                    producto.getStockMinimo(),
                    alerta,
                    lotesProximos.isEmpty() ? null : lotesProximos.get(0).getFechaVencimiento()
            });
        }

        return resultados;
    }

    // ==============================
    // MÉTODOS AUXILIARES (Conversión de datos)
    // ==============================

    private List<EstadisticasVentasResponse.DistribucionPago> convertirDistribucion(List<Object[]> data) {
        List<EstadisticasVentasResponse.DistribucionPago> list = new ArrayList<>();
        for (Object[] row : data) {
            list.add(EstadisticasVentasResponse.DistribucionPago.builder()
                    .medioPago((Venta.MedioPago) row[0])
                    .total((BigDecimal) row[1])
                    .cantidadTransacciones(((Number) row[2]).longValue())
                    .build());
        }
        return list;
    }

    private List<EstadisticasVentasResponse.ProductoTop> convertirTopProductos(List<Object[]> data) {
        List<EstadisticasVentasResponse.ProductoTop> list = new ArrayList<>();
        for (Object[] row : data) {
            list.add(EstadisticasVentasResponse.ProductoTop.builder()
                    .productoId((Integer) row[0])
                    .productoNombre((String) row[1])
                    .cantidadVendida(((Number) row[2]).intValue())
                    .totalFacturado((BigDecimal) row[3])
                    .build());
        }
        return list;
    }

    private List<EstadisticasVentasResponse.TendenciaDiaria> convertirTendencia(List<Object[]> data) {
        List<EstadisticasVentasResponse.TendenciaDiaria> list = new ArrayList<>();
        for (Object[] row : data) {
            list.add(EstadisticasVentasResponse.TendenciaDiaria.builder()
            		.fecha(((java.sql.Date) row[0]).toLocalDate())
                    .total((BigDecimal) row[1])
                    .build());
        }
        return list;
    }
}