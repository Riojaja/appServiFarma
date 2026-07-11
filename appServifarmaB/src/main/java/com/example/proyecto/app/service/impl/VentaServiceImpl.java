package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.DetalleVentaRequest;
import com.example.proyecto.app.dto.request.VentaRequest;
import com.example.proyecto.app.dto.response.DetalleVentaResponse;
import com.example.proyecto.app.dto.response.VentaResponse;
import com.example.proyecto.app.entity.*;
import com.example.proyecto.app.exception.*;
import com.example.proyecto.app.mapper.DetalleVentaMapper;
import com.example.proyecto.app.mapper.VentaMapper;
import com.example.proyecto.app.repository.*;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.InventarioService;
import com.example.proyecto.app.service.VentaService;
import com.example.proyecto.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaServiceImpl implements VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaServiceImpl.class);

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final CajaRepository cajaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final InventarioService inventarioService;
    private final VentaMapper ventaMapper;
    private final DetalleVentaMapper detalleVentaMapper;
    private final BitacoraComunicacionService bitacoraService;
    private final SecurityUtils securityUtils;

    // ==============================
    // OPERACIÓN PRINCIPAL: REGISTRAR VENTA
    // ==============================

    @Override
    @Transactional
    public VentaResponse registrarVenta(VentaRequest request) {
        log.info("Iniciando registro de venta para usuario ID: {}", request.getUsuarioId());

        // 1. Validar que la caja esté abierta
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(Caja.EstadoCaja.abierta)
                .orElseThrow(() -> new CajaCerradaException("No hay una caja abierta. No se puede registrar la venta."));

        // 2. Validar el usuario
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + request.getUsuarioId() + " no encontrado."));

        // 3. Validar el cliente (opcional)
        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID " + request.getClienteId() + " no encontrado."));
        }

        // 4. Validar que haya al menos un detalle en la venta
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new ParametroInvalidoException("La venta debe tener al menos un producto.");
        }

        // 5. Crear la venta (cabecera)
        Venta venta = Venta.builder()
                .fecha(LocalDateTime.now())
                .usuario(usuario)
                .cliente(cliente)
                .medioPago(request.getMedioPago())
                .codigoAutorizacion(request.getCodigoAutorizacion())
                .caja(caja)
                .estado(Venta.EstadoVenta.completada)
                .total(BigDecimal.ZERO) // Se calculará después
                .build();

        // Guardar la venta para obtener un ID (necesario para los detalles)
        Venta savedVenta = ventaRepository.save(venta);
        log.debug("Venta cabecera guardada con ID: {}", savedVenta.getId());

        // 6. Procesar los detalles, calcular total y descontar stock
        BigDecimal totalVenta = BigDecimal.ZERO;
        List<DetalleVenta> detallesGuardados = new ArrayList<>();

        for (DetalleVentaRequest detalleReq : request.getDetalles()) {
            // Validar producto
            Producto producto = productoRepository.findById(detalleReq.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + detalleReq.getProductoId() + " no encontrado."));

            // Obtener lote FEFO para el producto
            Lote lote = inventarioService.obtenerLoteFEFO(producto.getId());

            // Validar cantidad solicitada contra el stock del lote
            if (lote.getCantidad() < detalleReq.getCantidad()) {
                throw new StockInsuficienteException(
                        "Stock insuficiente para el producto: " + producto.getNombre() +
                        ". Disponible en lote FEFO: " + lote.getCantidad() + ", solicitado: " + detalleReq.getCantidad()
                );
            }

            // Calcular subtotal
            BigDecimal subtotal = lote.getPrecioVenta().multiply(BigDecimal.valueOf(detalleReq.getCantidad()));
            totalVenta = totalVenta.add(subtotal);

            // Crear detalle de venta
            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(savedVenta)
                    .lote(lote)
                    .cantidad(detalleReq.getCantidad())
                    .precioUnitarioVenta(lote.getPrecioVenta())
                    .precioCompraUnitario(lote.getPrecioCompra())
                    .build();

            detallesGuardados.add(detalle);
        }

        // 7. Guardar todos los detalles de una vez
        detalleVentaRepository.saveAll(detallesGuardados);
        log.debug("Se guardaron {} detalles para la venta ID: {}", detallesGuardados.size(), savedVenta.getId());

        // 8. Actualizar el total de la venta y guardar
        savedVenta.setTotal(totalVenta);
        Venta ventaFinal = ventaRepository.save(savedVenta);

        // 9. Descontar el stock de cada lote y registrar movimientos
        for (DetalleVenta detalle : detallesGuardados) {
            inventarioService.descontarStock(
                    detalle.getLote().getId(),
                    detalle.getCantidad(),
                    request.getUsuarioId(),
                    ventaFinal.getId()
            );
        }

        log.info("Venta registrada exitosamente. ID: {}, Total: {}", ventaFinal.getId(), totalVenta);

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            String clienteInfo = (cliente != null) ? " - Cliente: " + cliente.getNombre() : " - Cliente: General";
            String mensaje = String.format(
                    "🛒 Venta registrada - Total: S/ %.2f - Usuario: %s %s - Medio de pago: %s",
                    totalVenta,
                    usuario.getUsuario(),
                    clienteInfo,
                    request.getMedioPago().name()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuario.getId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para venta ID: {}", ventaFinal.getId());
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para venta: {}", e.getMessage());
        }

        return ventaMapper.toResponse(ventaFinal);
    }

    // ==============================
    // ANULACIÓN DE VENTAS
    // ==============================

    @Override
    @Transactional
    public void anularVenta(Integer id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + id + " no encontrada."));

        if (venta.getEstado() == Venta.EstadoVenta.anulada) {
            throw new VentaAnuladaException("La venta ya se encuentra anulada.");
        }

        // Opcional: Aquí se podría revertir el stock, pero la lógica de negocio puede no requerirlo.
        venta.setEstado(Venta.EstadoVenta.anulada);
        ventaRepository.save(venta);

        log.info("Venta anulada: ID {}", id);

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            // Obtener usuario autenticado o usar sistema
            Integer usuarioId = getUsuarioId();

            String mensaje = String.format(
                    "🚫 Venta anulada - ID: %d - Total: S/ %.2f",
                    id,
                    venta.getTotal()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuarioId)
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.incidencia)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para anulación de venta ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para anulación de venta: {}", e.getMessage());
        }
    }

    // ==============================
    // CONSULTAS DE VENTAS
    // ==============================

    @Override
    public VentaResponse obtenerVentaPorId(Integer id) {
        Venta venta = ventaRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + id + " no encontrada."));
        return ventaMapper.toResponse(venta);
    }

    @Override
    public List<VentaResponse> listarTodas() {
        return ventaRepository.findAll().stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponse> listarPorCliente(Integer clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResourceNotFoundException("Cliente con ID " + clienteId + " no encontrado.");
        }
        return ventaRepository.findByClienteId(clienteId).stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponse> listarPorUsuario(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }
        return ventaRepository.findByUsuarioId(usuarioId).stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return ventaRepository.findByFechaBetween(inicio, fin).stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponse> listarPorMedioPago(Venta.MedioPago medioPago) {
        return ventaRepository.findByMedioPago(medioPago).stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponse> listarPorEstado(Venta.EstadoVenta estado) {
        return ventaRepository.findByEstado(estado).stream()
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<VentaResponse> listarUltimasVentas(int limite) {
        return ventaRepository.findTopVentasRecientes(Venta.EstadoVenta.completada).stream()
                .limit(limite)
                .map(ventaMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // CONSULTAS DE AGREGACIÓN
    // ==============================

    @Override
    public BigDecimal obtenerTotalVentasPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return ventaRepository.sumTotalByFechaBetweenAndEstado(inicio, fin, Venta.EstadoVenta.completada);
    }

    @Override
    public List<Object[]> obtenerTotalVentasPorMedioPagoYPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        // Nota: Este método requiere una consulta personalizada en VentaRepository.
        throw new UnsupportedOperationException("Método pendiente de implementar: obtenerTotalVentasPorMedioPagoYPeriodo");
    }
    
    @Override
    public List<DetalleVentaResponse> obtenerDetallesVenta(Integer ventaId) {
        List<DetalleVenta> detalles = detalleVentaRepository.findByVentaId(ventaId);
        return detalles.stream()
                .map(detalleVentaMapper::toResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void eliminarVentasPorCliente(Integer clienteId) {
        List<Venta> ventas = ventaRepository.findByClienteId(clienteId);
        if (ventas.isEmpty()) {
            throw new ResourceNotFoundException("El cliente no tiene ventas para eliminar.");
        }

        // Obtener nombre del cliente para el mensaje (opcional)
        String nombreCliente = "Cliente ID: " + clienteId;
        try {
            Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
            if (cliente != null) {
                nombreCliente = cliente.getNombre();
            }
        } catch (Exception e) {
            // Ignorar
        }

        ventaRepository.deleteAll(ventas);
        log.info("Se eliminaron {} ventas del cliente ID: {}", ventas.size(), clienteId);

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            Integer usuarioId = getUsuarioId();
            String mensaje = String.format(
                    "🗑️ Eliminación masiva de ventas - Cliente: %s - Cantidad: %d",
                    nombreCliente,
                    ventas.size()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuarioId)
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.incidencia)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para eliminación masiva de ventas del cliente ID: {}", clienteId);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para eliminación de ventas: {}", e.getMessage());
        }
    }

    // ==============================
    // MÉTODO AUXILIAR PARA OBTENER USUARIO
    // ==============================

    private Integer getUsuarioId() {
        try {
            return securityUtils.getUsuarioAutenticado().getId();
        } catch (Exception e) {
            log.debug("No se pudo obtener usuario autenticado, usando usuario sistema (ID 1)");
            return 1; // Usuario sistema por defecto
        }
    }
}