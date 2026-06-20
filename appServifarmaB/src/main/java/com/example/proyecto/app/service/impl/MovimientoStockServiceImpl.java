package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.response.MovimientoStockResponse;
import com.example.proyecto.app.entity.MovimientoStock;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.MovimientoStockMapper;
import com.example.proyecto.app.repository.LoteRepository;
import com.example.proyecto.app.repository.MovimientoStockRepository;
import com.example.proyecto.app.repository.UsuarioRepository;
import com.example.proyecto.app.service.MovimientoStockService;
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
public class MovimientoStockServiceImpl implements MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final LoteRepository loteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoStockMapper movimientoStockMapper;

    @Override
    public List<MovimientoStockResponse> listarPorLote(Integer loteId) {
        if (!loteRepository.existsById(loteId)) {
            throw new ResourceNotFoundException("Lote con ID " + loteId + " no encontrado.");
        }
        log.debug("Obteniendo movimientos de stock para el lote ID: {}", loteId);
        return movimientoStockRepository.findByLoteIdOrderByFechaDesc(loteId).stream()
                .map(movimientoStockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockResponse> listarPorUsuario(Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResourceNotFoundException("Usuario con ID " + usuarioId + " no encontrado.");
        }
        log.debug("Obteniendo movimientos de stock para el usuario ID: {}", usuarioId);
        return movimientoStockRepository.findByUsuarioId(usuarioId).stream()
                .map(movimientoStockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockResponse> listarPorTipo(MovimientoStock.TipoMovimiento tipoMovimiento) {
        log.debug("Obteniendo movimientos de stock por tipo: {}", tipoMovimiento);
        return movimientoStockRepository.findByTipoMovimiento(tipoMovimiento).stream()
                .map(movimientoStockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
        log.debug("Obteniendo movimientos de stock entre {} y {}", inicio, fin);
        return movimientoStockRepository.findByFechaBetween(inicio, fin).stream()
                .map(movimientoStockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockResponse> listarTodos() {
        log.debug("Obteniendo todos los movimientos de stock");
        return movimientoStockRepository.findAll().stream()
                .map(movimientoStockMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MovimientoStockResponse> listarPorLoteYTipo(Integer loteId, MovimientoStock.TipoMovimiento tipoMovimiento) {
        if (!loteRepository.existsById(loteId)) {
            throw new ResourceNotFoundException("Lote con ID " + loteId + " no encontrado.");
        }
        log.debug("Obteniendo movimientos de stock para el lote ID: {} y tipo: {}", loteId, tipoMovimiento);
        return movimientoStockRepository.findByLoteIdAndTipoMovimiento(loteId, tipoMovimiento).stream()
                .map(movimientoStockMapper::toResponse)
                .collect(Collectors.toList());
    }
}