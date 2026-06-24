package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.entity.Caja;
import com.example.proyecto.app.entity.MovimientoStock;
import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.repository.*;
import com.example.proyecto.app.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaServiceImpl implements AuditoriaService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final VentaRepository ventaRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final LoteRepository loteRepository;

    // ==============================
    // AUDITORÍA DE MOVIMIENTOS DE STOCK
    // ==============================

    @Override
    public List<MovimientoStock> obtenerMovimientosPorUsuario(Integer usuarioId, LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo movimientos de stock para usuario ID: {} entre {} y {}", usuarioId, inicio, fin);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }

        return movimientoStockRepository.findByUsuarioId(usuarioId).stream()
                .filter(m -> !m.getFecha().isBefore(inicio) && !m.getFecha().isAfter(fin))
                .sorted(Comparator.comparing(MovimientoStock::getFecha).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStock> obtenerMovimientosPorTipo(MovimientoStock.TipoMovimiento tipo, LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo movimientos de stock por tipo: {} entre {} y {}", tipo, inicio, fin);

        return movimientoStockRepository.findByTipoMovimiento(tipo).stream()
                .filter(m -> !m.getFecha().isBefore(inicio) && !m.getFecha().isAfter(fin))
                .sorted(Comparator.comparing(MovimientoStock::getFecha).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStock> obtenerHistorialLote(Integer loteId) {
        log.debug("Obteniendo historial completo del lote ID: {}", loteId);

        if (!loteRepository.existsById(loteId)) {
            throw new ResourceNotFoundException("Lote con ID " + loteId + " no encontrado.");
        }

        return movimientoStockRepository.findByLoteIdOrderByFechaDesc(loteId);
    }

    // ==============================
    // AUDITORÍA DE VENTAS
    // ==============================

    @Override
    public List<Venta> obtenerVentasAnuladas(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo ventas anuladas entre {} y {}", inicio, fin);

        return ventaRepository.findByEstado(Venta.EstadoVenta.anulada).stream()
                .filter(v -> !v.getFecha().isBefore(inicio) && !v.getFecha().isAfter(fin))
                .sorted(Comparator.comparing(Venta::getFecha).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Venta> obtenerVentasPorUsuario(Integer usuarioId, LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo ventas para usuario ID: {} entre {} y {}", usuarioId, inicio, fin);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }

        return ventaRepository.findByUsuarioId(usuarioId).stream()
                .filter(v -> !v.getFecha().isBefore(inicio) && !v.getFecha().isAfter(fin))
                .sorted(Comparator.comparing(Venta::getFecha).reversed())
                .collect(Collectors.toList());
    }

    // ==============================
    // AUDITORÍA DE CAJA
    // ==============================

    @Override
    public List<Map<String, Object>> obtenerHistorialCajaPorUsuario(Integer usuarioId) {
        log.debug("Obteniendo historial de caja para usuario ID: {}", usuarioId);

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }

        List<Caja> cajas = cajaRepository.findByUsuarioAperturaId(usuarioId);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Caja caja : cajas) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("cajaId", caja.getId());
            item.put("fechaApertura", caja.getFechaApertura());
            item.put("fechaCierre", caja.getFechaCierre());
            item.put("montoApertura", caja.getMontoApertura());
            item.put("montoCierreDeclarado", caja.getMontoCierreDeclarado());
            item.put("estado", caja.getEstado());

            if (caja.getEstado() == Caja.EstadoCaja.cerrada) {
                BigDecimal totalVentas = ventaRepository.sumTotalByCajaIdAndEstado(
                        caja.getId(), Venta.EstadoVenta.completada);
                item.put("totalVentas", totalVentas);
                if (caja.getMontoCierreDeclarado() != null) {
                    item.put("diferencia", caja.getMontoCierreDeclarado().subtract(totalVentas));
                }
            }

            resultado.add(item);
        }

        resultado.sort(Comparator.comparing(m -> (LocalDateTime) m.get("fechaApertura"), Comparator.reverseOrder()));
        return resultado;
    }

    @Override
    public List<Map<String, Object>> obtenerCajasConDiferencia(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo cajas con diferencia (sobrante/faltante) entre {} y {}", inicio, fin);

        List<Caja> cajas = cajaRepository.findByFechaCierreBetween(inicio, fin);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Caja caja : cajas) {
            if (caja.getEstado() != Caja.EstadoCaja.cerrada || caja.getMontoCierreDeclarado() == null) {
                continue;
            }

            BigDecimal totalVentas = ventaRepository.sumTotalByCajaIdAndEstado(
                    caja.getId(), Venta.EstadoVenta.completada);
            BigDecimal diferencia = caja.getMontoCierreDeclarado().subtract(totalVentas);

            if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("cajaId", caja.getId());
                item.put("fechaApertura", caja.getFechaApertura());
                item.put("fechaCierre", caja.getFechaCierre());  // <-- CORREGIDO: antes era "fechaCierra"
                item.put("totalVentas", totalVentas);
                item.put("montoDeclarado", caja.getMontoCierreDeclarado());
                item.put("diferencia", diferencia);
                item.put("tipo", diferencia.compareTo(BigDecimal.ZERO) > 0 ? "SOBRANTE" : "FALTANTE");
                resultado.add(item);
            }
        }

        resultado.sort(Comparator.comparing(m -> (LocalDateTime) m.get("fechaCierre"), Comparator.reverseOrder())); // CORREGIDO
        return resultado;
    }

    // ==============================
    // AUDITORÍA GENERAL
    // ==============================

    @Override
    public Map<String, Object> obtenerResumenActividad(LocalDateTime inicio, LocalDateTime fin) {
        log.debug("Obteniendo resumen de actividad entre {} y {}", inicio, fin);

        Map<String, Object> resumen = new LinkedHashMap<>();

        BigDecimal totalVentas = ventaRepository.sumTotalByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.completada);
        Long totalTransacciones = ventaRepository.countByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.completada);

        Long totalAnuladas = ventaRepository.countByFechaBetweenAndEstado(
                inicio, fin, Venta.EstadoVenta.anulada);

        List<MovimientoStock> movimientos = movimientoStockRepository.findByFechaBetween(inicio, fin);
        Long totalMovimientos = (long) movimientos.size();

        List<Caja> cajas = cajaRepository.findByFechaCierreBetween(inicio, fin);
        Long totalCajas = (long) cajas.size();

        Map<MovimientoStock.TipoMovimiento, Long> movimientosPorTipo = movimientos.stream()
                .collect(Collectors.groupingBy(MovimientoStock::getTipoMovimiento, Collectors.counting()));

        resumen.put("fechaInicio", inicio);
        resumen.put("fechaFin", fin);
        resumen.put("totalVentas", totalVentas);
        resumen.put("totalTransacciones", totalTransacciones);
        resumen.put("totalAnuladas", totalAnuladas);
        resumen.put("totalMovimientosStock", totalMovimientos);
        resumen.put("movimientosPorTipo", movimientosPorTipo);
        resumen.put("totalCajasCerradas", totalCajas);

        return resumen;
    }
}