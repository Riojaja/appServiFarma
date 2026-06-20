package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.entity.Lote;
import com.example.proyecto.app.entity.MovimientoStock;
import com.example.proyecto.app.entity.Producto;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.LoteVencidoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.exception.StockInsuficienteException;
import com.example.proyecto.app.repository.LoteRepository;
import com.example.proyecto.app.repository.MovimientoStockRepository;
import com.example.proyecto.app.repository.ParametroSistemaRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioServiceImpl implements InventarioService {

    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final ParametroSistemaRepository parametroSistemaRepository;

    // ==============================
    // CÁLCULO DE STOCK
    // ==============================

    @Override
    public Integer calcularStockProducto(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado.");
        }

        log.debug("Calculando stock total para producto ID: {}", productoId);
        return loteRepository.sumCantidadByProductoIdAndEstado(productoId, Lote.EstadoLote.activo);
    }

    // ==============================
    // LÓGICA FEFO (First Expired, First Out)
    // ==============================

    @Override
    public Lote obtenerLoteFEFO(Integer productoId) {
        if (!productoRepository.existsById(productoId)) {
            throw new ResourceNotFoundException("Producto con ID " + productoId + " no encontrado.");
        }

        log.debug("Buscando lote FEFO para producto ID: {}", productoId);
        return loteRepository
                .findFirstByProductoIdAndEstadoOrderByFechaVencimientoAsc(productoId, Lote.EstadoLote.activo)
                .orElseThrow(() -> new StockInsuficienteException(
                        "No hay stock disponible (lotes activos) para el producto con ID: " + productoId
                ));
    }

    // ==============================
    // DESCUENTO DE STOCK
    // ==============================

    @Override
    @Transactional
    public void descontarStock(Integer loteId, Integer cantidad, Integer usuarioId, Integer referenciaId) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }

        // 1. Obtener el lote
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote con ID " + loteId + " no encontrado."));

        log.debug("Descontando {} unidades del lote ID: {} (stock actual: {})", cantidad, loteId, lote.getCantidad());

        // 2. Validar que el lote no esté vencido
        if (lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new LoteVencidoException("No se puede descontar stock de un lote vencido (ID: " + loteId + ")");
        }

        // 3. Validar stock suficiente
        if (lote.getCantidad() < cantidad) {
            throw new StockInsuficienteException(
                    "Stock insuficiente en lote ID: " + loteId + ". Disponible: " + lote.getCantidad() + ", solicitado: " + cantidad
            );
        }

        // 4. Descontar cantidad
        lote.setCantidad(lote.getCantidad() - cantidad);
        loteRepository.save(lote);

        // 5. Registrar movimiento de stock
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado."));

        MovimientoStock movimiento = MovimientoStock.builder()
                .lote(lote)
                .usuario(usuario)
                .tipoMovimiento(MovimientoStock.TipoMovimiento.venta)
                .cantidad(cantidad)
                .costoUnitario(lote.getPrecioCompra())
                .referenciaId(referenciaId)
                .observacion("Descuento por venta ID: " + referenciaId)
                .build();

        movimientoStockRepository.save(movimiento);
        log.info("Movimiento de stock registrado para venta ID: {}", referenciaId);

        // 6. Si el lote quedó en 0, actualizar estado a 'agotado'
        if (lote.getCantidad() == 0) {
            int updated = loteRepository.actualizarEstadoAAgotado(lote.getId(), Lote.EstadoLote.agotado);
            if (updated > 0) {
                log.info("Lote ID: {} actualizado a estado 'agotado'", lote.getId());
            }
        }
    }

    // ==============================
    // ACTUALIZACIÓN DE ESTADOS DE LOTES (Tareas programadas)
    // ==============================

    @Override
    @Transactional
    public int actualizarLotesVencidos() {
        log.info("Iniciando actualización de lotes vencidos");
        int updated = loteRepository.marcarLotesVencidos(LocalDate.now(), Lote.EstadoLote.vencido);
        log.info("Se actualizaron {} lotes a estado 'vencido'", updated);
        return updated;
    }

    @Override
    @Transactional
    public int actualizarLotesAgotados() {
        log.info("Iniciando actualización de lotes agotados");
        int count = 0;
        List<Lote> lotesAgotados = loteRepository.findByEstadoAndCantidad(Lote.EstadoLote.activo, 0);
        for (Lote lote : lotesAgotados) {
            int updated = loteRepository.actualizarEstadoAAgotado(lote.getId(), Lote.EstadoLote.agotado);
            if (updated > 0) {
                count++;
            }
        }
        log.info("Se actualizaron {} lotes a estado 'agotado'", count);
        return count;
    }
    // ==============================
    // GENERACIÓN DE ALERTAS (usando ParametroSistema)
    // ==============================

    @Override
    public List<Integer> obtenerProductosConStockBajo() {
        log.debug("Obteniendo productos con stock bajo");
        return productoRepository.findProductosConStockBajo().stream()
                .map(Producto::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Lote> obtenerLotesProximosAVencer() {
        // Obtener días de anticipación desde ParametroSistema
        String diasStr = parametroSistemaRepository.findValorByClave("dias_alerta_vencimiento")
                .orElse("30"); // Valor por defecto: 30 días

        int diasAnticipacion;
        try {
            diasAnticipacion = Integer.parseInt(diasStr);
        } catch (NumberFormatException e) {
            log.warn("Valor inválido para 'dias_alerta_vencimiento': {}, usando 30 por defecto", diasStr);
            diasAnticipacion = 30;
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaLimite = hoy.plusDays(diasAnticipacion);

        log.debug("Buscando lotes próximos a vencer (hasta {} días)", diasAnticipacion);
        return loteRepository.findByFechaVencimientoBetweenAndEstado(hoy, fechaLimite, Lote.EstadoLote.activo);
    }

    @Override
    public List<Lote> obtenerLotesActivosConStock() {
        log.debug("Obteniendo lotes activos con stock disponible");
        return loteRepository.findByEstadoAndCantidadGreaterThan(Lote.EstadoLote.activo, 0);
    }
}
