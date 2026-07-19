package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ParametroSistemaRequest;
import com.example.proyecto.app.dto.response.ParametroSistemaResponse;
import com.example.proyecto.app.entity.ParametroSistema;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ParametroSistemaMapper;
import com.example.proyecto.app.repository.ParametroSistemaRepository;
import com.example.proyecto.app.service.ParametroSistemaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParametroSistemaServiceImpl implements ParametroSistemaService {

    private final ParametroSistemaRepository parametroRepository;
    private final ParametroSistemaMapper mapper;

    private static final Pattern ENV_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Resuelve variables de entorno en formato ${VARIABLE}.
     * Si el valor no tiene el formato ${...}, lo devuelve tal cual.
     */
    private String resolverVariable(String valor) {
        if (valor == null) return null;
        Matcher matcher = ENV_PATTERN.matcher(valor);
        if (matcher.matches()) {
            String varName = matcher.group(1);
            String envValue = System.getenv(varName);
            if (envValue != null && !envValue.isEmpty()) {
                log.debug("✅ Variable de entorno {} resuelta correctamente", varName);
                return envValue;
            }
            log.warn("⚠️ Variable de entorno {} no encontrada, usando valor literal {}", varName, valor);
            return valor; // fallback: devuelve el valor original
        }
        return valor;
    }

    // ============================================================
    // MÉTODO CLAVE PARA SMTP
    // ============================================================
    @Override
    public String obtenerValorPorClave(String clave) {
        return parametroRepository.findValorByClave(clave)
                .map(this::resolverVariable)
                .orElse(null);
    }

    // ============================================================
    // OTROS MÉTODOS (implementación estándar)
    // ============================================================

    @Override
    @Transactional
    public ParametroSistemaResponse crearParametro(ParametroSistemaRequest request) {
        if (parametroRepository.existsByClave(request.getClave())) {
            throw new IllegalArgumentException("Ya existe un parámetro con la clave: " + request.getClave());
        }
        ParametroSistema entity = mapper.toEntity(request);
        ParametroSistema saved = parametroRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ParametroSistemaResponse actualizarParametro(Integer id, ParametroSistemaRequest request) {
        ParametroSistema entity = parametroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro no encontrado con ID: " + id));
        entity.setValor(request.getValor());
        entity.setDescripcion(request.getDescripcion());
        // No se actualiza la clave, porque es única y no debería cambiarse
        ParametroSistema updated = parametroRepository.save(entity);
        return mapper.toResponse(updated);
    }

    @Override
    public ParametroSistemaResponse obtenerPorId(Integer id) {
        ParametroSistema entity = parametroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro no encontrado con ID: " + id));
        return mapper.toResponse(entity);
    }

    @Override
    public ParametroSistemaResponse obtenerPorClave(String clave) {
        ParametroSistema entity = parametroRepository.findByClave(clave)
                .orElseThrow(() -> new ResourceNotFoundException("Parámetro no encontrado con clave: " + clave));
        // También resolvemos la variable si está en la entidad
        entity.setValor(resolverVariable(entity.getValor()));
        return mapper.toResponse(entity);
    }

    @Override
    public List<ParametroSistemaResponse> listarTodos() {
        return parametroRepository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorClave(String clave) {
        return parametroRepository.existsByClave(clave);
    }

    @Override
    @Transactional
    public void actualizarValorPorClave(String clave, String nuevoValor) {
        int updated = parametroRepository.updateValorByClave(clave, nuevoValor);
        if (updated == 0) {
            throw new ResourceNotFoundException("Parámetro no encontrado con clave: " + clave);
        }
    }

    @Override
    @Transactional
    public void eliminarParametro(Integer id) {
        if (!parametroRepository.existsById(id)) {
            throw new ResourceNotFoundException("Parámetro no encontrado con ID: " + id);
        }
        parametroRepository.deleteById(id);
    }
}