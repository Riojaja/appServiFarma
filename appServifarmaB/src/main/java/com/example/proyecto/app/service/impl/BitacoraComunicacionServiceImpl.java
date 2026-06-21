package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.BitacoraComunicacionRequest;
import com.example.proyecto.app.dto.response.BitacoraComunicacionResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Usuario;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.BitacoraComunicacionMapper;
import com.example.proyecto.app.repository.BitacoraComunicacionRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BitacoraComunicacionServiceImpl implements BitacoraComunicacionService {

    private final BitacoraComunicacionRepository bitacoraRepository;
    private final UsuarioRepository usuarioRepository;
    private final BitacoraComunicacionMapper bitacoraMapper;

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    @Override
    @Transactional
    public BitacoraComunicacionResponse crearMensaje(BitacoraComunicacionRequest request) {
        // 1. Validar que el usuario exista
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + request.getUsuarioId() + " no encontrado."));

        // 2. Construir la entidad (la fechaHora se asigna automáticamente con @CreationTimestamp)
        BitacoraComunicacion mensaje = BitacoraComunicacion.builder()
                .usuario(usuario)
                .mensaje(request.getMensaje())
                .tipo(request.getTipo())
                .leido(false) // Por defecto, no leído
                .build();

        // 3. Guardar
        BitacoraComunicacion saved = bitacoraRepository.save(mensaje);
        log.info("Mensaje creado en bitácora: ID {}, Usuario: {}, Tipo: {}",
                saved.getId(), usuario.getUsuario(), saved.getTipo());

        return bitacoraMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void marcarComoLeido(Integer id) {
        BitacoraComunicacion mensaje = bitacoraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje con ID " + id + " no encontrado."));

        mensaje.setLeido(true);
        bitacoraRepository.save(mensaje);
        log.debug("Mensaje marcado como leído: ID {}", id);
    }

    // ==============================
    // CONSULTAS
    // ==============================

    @Override
    public BitacoraComunicacionResponse obtenerPorId(Integer id) {
        BitacoraComunicacion mensaje = bitacoraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje con ID " + id + " no encontrado."));
        return bitacoraMapper.toResponse(mensaje);
    }

    @Override
    public List<BitacoraComunicacionResponse> listarTodos() {
        return bitacoraRepository.findAllByOrderByFechaHoraDesc().stream()
                .map(bitacoraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraComunicacionResponse> listarNoLeidos() {
        return bitacoraRepository.findByLeidoFalse().stream()
                .sorted(Comparator.comparing(BitacoraComunicacion::getFechaHora).reversed())
                .map(bitacoraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraComunicacionResponse> listarPorUsuario(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }
        return bitacoraRepository.findByUsuarioId(usuarioId).stream()
                .sorted(Comparator.comparing(BitacoraComunicacion::getFechaHora).reversed())
                .map(bitacoraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraComunicacionResponse> listarPorTipo(BitacoraComunicacion.Tipo tipo) {
        return bitacoraRepository.findByTipo(tipo).stream()
                .sorted(Comparator.comparing(BitacoraComunicacion::getFechaHora).reversed())
                .map(bitacoraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraComunicacionResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        return bitacoraRepository.findByFechaHoraBetween(inicio, fin).stream()
                .sorted(Comparator.comparing(BitacoraComunicacion::getFechaHora).reversed())
                .map(bitacoraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BitacoraComunicacionResponse> listarNoLeidosPorUsuario(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }
        return bitacoraRepository.findByUsuarioIdAndLeidoFalse(usuarioId).stream()
                .sorted(Comparator.comparing(BitacoraComunicacion::getFechaHora).reversed())
                .map(bitacoraMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    @Override
    @Transactional
    public void eliminarMensaje(Integer id) {
        if (!bitacoraRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mensaje con ID " + id + " no encontrado.");
        }
        bitacoraRepository.deleteById(id);
        log.info("Mensaje eliminado de la bitácora: ID {}", id);
    }
}