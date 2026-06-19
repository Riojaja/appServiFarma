package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ParametroSistemaRequest;
import com.example.proyecto.app.dto.response.ParametroSistemaResponse;
import com.example.proyecto.app.entity.ParametroSistema;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ParametroSistemaMapper;
import com.example.proyecto.app.repository.ParametroSistemaRepository;
import com.example.proyecto.app.service.ParametroSistemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParametroSistemaServiceImpl implements ParametroSistemaService {

    private final ParametroSistemaRepository parametroSistemaRepository;
    private final ParametroSistemaMapper parametroSistemaMapper;

    @Override
    @Transactional
    public ParametroSistemaResponse crearParametro(ParametroSistemaRequest request) {
        // Validar que la clave no exista
        if (parametroSistemaRepository.existsByClave(request.getClave())) {
            throw new DuplicadoException("Ya existe un parámetro con la clave: " + request.getClave());
        }

        ParametroSistema entity = parametroSistemaMapper.toEntity(request);
        ParametroSistema saved = parametroSistemaRepository.save(entity);
        return parametroSistemaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ParametroSistemaResponse actualizarParametro(Integer id, ParametroSistemaRequest request) {
        ParametroSistema entity = parametroSistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro con ID " + id + " no encontrado."));

        // Validar que la clave no esté siendo usada por otro parámetro
        if (!entity.getClave().equals(request.getClave()) &&
                parametroSistemaRepository.existsByClave(request.getClave())) {
            throw new DuplicadoException("La clave '" + request.getClave() + "' ya está registrada.");
        }

        entity.setClave(request.getClave());
        entity.setValor(request.getValor());
        entity.setDescripcion(request.getDescripcion());

        ParametroSistema updated = parametroSistemaRepository.save(entity);
        return parametroSistemaMapper.toResponse(updated);
    }

    @Override
    public ParametroSistemaResponse obtenerPorId(Integer id) {
        ParametroSistema entity = parametroSistemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro con ID " + id + " no encontrado."));
        return parametroSistemaMapper.toResponse(entity);
    }

    @Override
    public ParametroSistemaResponse obtenerPorClave(String clave) {
        ParametroSistema entity = parametroSistemaRepository.findByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro con clave '" + clave + "' no encontrado."));
        return parametroSistemaMapper.toResponse(entity);
    }

    @Override
    public String obtenerValorPorClave(String clave) {
        return parametroSistemaRepository.findValorByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro con clave '" + clave + "' no encontrado."));
    }

    @Override
    public List<ParametroSistemaResponse> listarTodos() {
        return parametroSistemaRepository.findAll().stream()
                .map(parametroSistemaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorClave(String clave) {
        return parametroSistemaRepository.existsByClave(clave);
    }

    @Override
    @Transactional
    public void actualizarValorPorClave(String clave, String nuevoValor) {
        int updatedRows = parametroSistemaRepository.updateValorByClave(clave, nuevoValor);
        if (updatedRows == 0) {
            throw new ResourceNotFoundException("Parámetro con clave '" + clave + "' no encontrado.");
        }
    }

    @Override
    @Transactional
    public void eliminarParametro(Integer id) {
        if (!parametroSistemaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parámetro con ID " + id + " no encontrado.");
        }
        parametroSistemaRepository.deleteById(id);
    }
}