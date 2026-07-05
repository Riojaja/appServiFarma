package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.AperturaCajaRequest;
import com.example.proyecto.app.dto.request.CierreCajaRequest;
import com.example.proyecto.app.dto.response.CajaResponse;
import com.example.proyecto.app.dto.response.CierreCajaResponse;
import com.example.proyecto.app.entity.Caja;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.entity.Venta;
import com.example.proyecto.app.exception.CajaCerradaException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.CajaMapper;
import com.example.proyecto.app.repository.CajaRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.repository.VentaRepository;
import com.example.proyecto.app.service.CajaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final CajaMapper cajaMapper;

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

        // 3. Validar que no haya ninguna caja abierta en el sistema (opcional, según lógica de negocio)
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
        // Nota: Este método depende de una consulta personalizada en VentaRepository.
        // Si no existe, se puede implementar o adaptar según la necesidad.
        // Se deja como placeholder para que se pueda implementar en el futuro.
        throw new UnsupportedOperationException("Método no implementado aún: obtenerTotalVentasPorMedioPago");
    }
}
