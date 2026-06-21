package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.DemandaInsatisfechaRequest;
import com.example.proyecto.app.dto.response.DemandaInsatisfechaResponse;
import com.example.proyecto.app.entity.DemandaInsatisfecha;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.DemandaInsatisfechaMapper;
import com.example.proyecto.app.repository.DemandaInsatisfechaRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.service.DemandaInsatisfechaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DemandaInsatisfechaServiceImpl implements DemandaInsatisfechaService {

    private final DemandaInsatisfechaRepository demandaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DemandaInsatisfechaMapper demandaMapper;

    // ==============================
    // OPERACIONES DE REGISTRO
    // ==============================

    @Override
    @Transactional
    public DemandaInsatisfechaResponse crearDemanda(DemandaInsatisfechaRequest request) {
        // 1. Validar que el usuario exista
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + request.getUsuarioId() + " no encontrado."));

        // 2. Crear la entidad
        DemandaInsatisfecha demanda = DemandaInsatisfecha.builder()
                .productoSolicitado(request.getProductoSolicitado())
                .fecha(LocalDateTime.now())
                .clienteDocumento(request.getClienteDocumento())
                .usuario(usuario)
                .build();

        // 3. Guardar
        DemandaInsatisfecha saved = demandaRepository.save(demanda);
        log.info("Demanda insatisfecha registrada: ID {}, Producto: {}, Usuario: {}",
                saved.getId(), saved.getProductoSolicitado(), usuario.getUsuario());

        return demandaMapper.toResponse(saved);
    }

    // ==============================
    // CONSULTAS
    // ==============================

    @Override
    public DemandaInsatisfechaResponse obtenerPorId(Integer id) {
        DemandaInsatisfecha demanda = demandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demanda insatisfecha con ID " + id + " no encontrada."));
        return demandaMapper.toResponse(demanda);
    }

    @Override
    public List<DemandaInsatisfechaResponse> listarTodas() {
        return demandaRepository.findAll().stream()
                .map(demandaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandaInsatisfechaResponse> listarPorUsuario(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }
        return demandaRepository.findByUsuarioId(usuarioId).stream()
                .map(demandaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandaInsatisfechaResponse> listarPorProducto(String productoSolicitado) {
        if (productoSolicitado == null || productoSolicitado.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío.");
        }
        return demandaRepository.findByProductoSolicitadoContainingIgnoreCase(productoSolicitado).stream()
                .map(demandaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandaInsatisfechaResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return demandaRepository.findByFechaBetween(inicio, fin).stream()
                .map(demandaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DemandaInsatisfechaResponse> listarPorClienteDocumento(String clienteDocumento) {
        if (clienteDocumento == null || clienteDocumento.isBlank()) {
            throw new IllegalArgumentException("El documento del cliente no puede estar vacío.");
        }
        return demandaRepository.findByClienteDocumento(clienteDocumento).stream()
                .map(demandaMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // ESTADÍSTICAS Y ELIMINACIÓN
    // ==============================

    @Override
    public long contarDemandasPorPeriodo(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return demandaRepository.countByFechaBetween(inicio, fin);
    }

    @Override
    @Transactional
    public void eliminarDemanda(Integer id) {
        if (!demandaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Demanda insatisfecha con ID " + id + " no encontrada.");
        }
        demandaRepository.deleteById(id);
        log.info("Demanda insatisfecha eliminada: ID {}", id);
    }
}
