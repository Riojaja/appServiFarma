package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.AperturaCajaRequest;
import com.example.proyecto.app.dto.request.CierreCajaRequest;
import com.example.proyecto.app.dto.response.CajaResponse;
import com.example.proyecto.app.dto.response.CierreCajaResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Caja;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.exception.CajaCerradaException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.CajaMapper;
import com.example.proyecto.app.repository.CajaRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.repository.VentaRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.CajaService;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CajaServiceImpl implements CajaService {

    private static final Logger log = LoggerFactory.getLogger(CajaServiceImpl.class);

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final CajaMapper cajaMapper;
    private final BitacoraComunicacionService bitacoraService;
 

    // ==============================
    // OPERACIONES DE APERTURA Y CIERRE
    // ==============================

    @Override
    @Transactional
    public CajaResponse abrirCaja(AperturaCajaRequest request) {
        // 1. Validar que el usuario exista
        Usuario usuario = usuarioRepository.findById(request.getUsuarioAperturaId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + request.getUsuarioAperturaId() + " no encontrado."));

        // 2. Validar que no haya una caja abierta para el mismo usuario
        if (cajaRepository.existsByUsuarioAperturaIdAndEstado(request.getUsuarioAperturaId(), Caja.EstadoCaja.abierta)) {
            throw new CajaCerradaException("El usuario ya tiene una caja abierta. Debe cerrarla antes de abrir otra.");
        }

        // 3. Validar que no haya ninguna caja abierta en el sistema
        if (cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(Caja.EstadoCaja.abierta).isPresent()) {
            throw new CajaCerradaException("Ya existe una caja abierta en el sistema. Solo puede haber una caja abierta a la vez.");
        }

        // 4. Crear la nueva caja
        Caja caja = Caja.builder()
                .fechaApertura(LocalDateTime.now())
                .montoApertura(request.getMontoApertura())
                .usuarioApertura(usuario)
                .estado(Caja.EstadoCaja.abierta)
                .build();

        Caja saved = cajaRepository.save(caja);
        log.info("Caja abierta: ID {}, Usuario: {}, Monto inicial: {}",
                saved.getId(), usuario.getUsuario(), saved.getMontoApertura());

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "💰 Caja abierta por %s - Monto inicial: S/ %.2f",
                    usuario.getUsuario(),
                    saved.getMontoApertura()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuario.getId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para apertura de caja #{}", saved.getId());
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para apertura de caja: {}", e.getMessage());
            // No propagamos la excepción para no interrumpir el flujo principal
        }

        return cajaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CierreCajaResponse cerrarCaja(CierreCajaRequest request) {
        // 1. Validar que el usuario exista
        Usuario usuario = usuarioRepository.findById(request.getUsuarioCierreId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + request.getUsuarioCierreId() + " no encontrado."));

        // 2. Obtener la caja abierta más reciente
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(Caja.EstadoCaja.abierta)
                .orElseThrow(() -> new CajaCerradaException("No hay una caja abierta para cerrar."));

        // 3. Calcular total de ventas (solo ventas completadas)
        BigDecimal totalVentas = ventaRepository.sumTotalByCajaIdAndEstado(caja.getId(), Venta.EstadoVenta.completada);

        // 4. Calcular diferencias
        BigDecimal diferencia = request.getMontoCierreDeclarado().subtract(totalVentas);

        // 5. Cerrar la caja
        caja.setFechaCierre(LocalDateTime.now());
        caja.setMontoCierreDeclarado(request.getMontoCierreDeclarado());
        caja.setUsuarioCierre(usuario);
        caja.setEstado(Caja.EstadoCaja.cerrada);

        Caja saved = cajaRepository.save(caja);
        log.info("Caja cerrada: ID {}, Usuario: {}, Total ventas: {}, Monto declarado: {}, Diferencia: {}",
                saved.getId(), usuario.getUsuario(), totalVentas, request.getMontoCierreDeclarado(), diferencia);

        // ==============================================================
        // CREAR MENSAJE EN BITÁCORA
        // ==============================================================
        try {
            String diferenciaTexto = diferencia.compareTo(BigDecimal.ZERO) >= 0
                    ? "(Sobrante: S/ " + diferencia.abs() + ")"
                    : "(Faltante: S/ " + diferencia.abs() + ")";

            // Si la diferencia es mayor a 5 soles, se considera incidencia, sino novedad
            BitacoraComunicacion.Tipo tipo = diferencia.abs().compareTo(new BigDecimal("5")) > 0
                    ? BitacoraComunicacion.Tipo.incidencia
                    : BitacoraComunicacion.Tipo.novedad;

            String mensaje = String.format(
                    "🔒 Caja cerrada por %s - Total ventas: S/ %.2f - Monto declarado: S/ %.2f - Diferencia: S/ %.2f %s",
                    usuario.getUsuario(),
                    totalVentas,
                    request.getMontoCierreDeclarado(),
                    diferencia.abs(),
                    diferenciaTexto
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(usuario.getId())
                            .mensaje(mensaje)
                            .tipo(tipo)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para cierre de caja #{}", saved.getId());
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para cierre de caja: {}", e.getMessage());
        }

        // 6. Construir respuesta
        return CierreCajaResponse.builder()
                .cajaId(saved.getId())
                .fechaApertura(saved.getFechaApertura())
                .fechaCierre(saved.getFechaCierre())
                .montoApertura(saved.getMontoApertura())
                .totalVentas(totalVentas)
                .montoDeclarado(request.getMontoCierreDeclarado())
                .diferencia(diferencia)
                .usuarioApertura(saved.getUsuarioApertura().getNombreCompleto())
                .usuarioCierre(usuario.getNombreCompleto())
                .build();
    }

    // ==============================
    // CONSULTAS DE CAJA
    // ==============================

    @Override
    public CajaResponse obtenerCajaAbierta() {
        Caja caja = cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(Caja.EstadoCaja.abierta)
                .orElseThrow(() -> new CajaCerradaException("No hay una caja abierta en el sistema."));
        return cajaMapper.toResponse(caja);
    }

    @Override
    public CajaResponse obtenerCajaPorId(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja con ID " + id + " no encontrada."));
        return cajaMapper.toResponse(caja);
    }

    @Override
    public List<CajaResponse> listarTodas() {
        return cajaRepository.findAllByOrderByFechaAperturaDesc().stream()
                .map(cajaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CajaResponse> listarPorUsuarioApertura(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }
        return cajaRepository.findByUsuarioAperturaId(usuarioId).stream()
                .map(cajaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CajaResponse> listarPorFechasCierre(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return cajaRepository.findByFechaCierreBetween(inicio, fin).stream()
                .map(cajaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CajaResponse> listarPorEstado(Caja.EstadoCaja estado) {
        return cajaRepository.findByEstado(estado).stream()
                .map(cajaMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // VALIDACIONES Y CONSULTAS DE ESTADO
    // ==============================

    @Override
    public boolean existeCajaAbierta() {
        return cajaRepository.findFirstByEstadoOrderByFechaAperturaDesc(Caja.EstadoCaja.abierta).isPresent();
    }

    @Override
    public boolean usuarioTieneCajaAbierta(Integer usuarioId) {
        return cajaRepository.existsByUsuarioAperturaIdAndEstado(usuarioId, Caja.EstadoCaja.abierta);
    }

    @Override
    public BigDecimal obtenerTotalVentasCaja(Integer cajaId) {
        if (!cajaRepository.existsById(cajaId)) {
            throw new ResourceNotFoundException("Caja con ID " + cajaId + " no encontrada.");
        }
        return ventaRepository.sumTotalByCajaIdAndEstado(cajaId, Venta.EstadoVenta.completada);
    }

    @Override
    public BigDecimal obtenerTotalVentasPorMedioPago(Integer cajaId, Venta.MedioPago medioPago) {
        if (!cajaRepository.existsById(cajaId)) {
            throw new ResourceNotFoundException("Caja con ID " + cajaId + " no encontrada.");
        }
        throw new UnsupportedOperationException("Método no implementado aún: obtenerTotalVentasPorMedioPago");
    }
}