package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.entity.Configuracion;
import com.example.proyecto.app.repository.ConfiguracionRepository;
import com.example.proyecto.app.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConfiguracionServiceImpl implements ConfiguracionService {

    private static final Logger log = LoggerFactory.getLogger(ConfiguracionServiceImpl.class);
    private final ConfiguracionRepository configuracionRepository;

    @Override
    public String getValor(String clave) {
        return configuracionRepository.findById(clave)
                .map(Configuracion::getValor)
                .orElse(null);
    }

    @Override
    public Integer getValorInteger(String clave) {
        String valor = getValor(clave);
        try {
            return valor != null ? Integer.parseInt(valor) : null;
        } catch (NumberFormatException e) {
            log.warn("Configuración {} no es un número válido: {}", clave, valor);
            return null;
        }
    }

    @Override
    @Transactional
    public void setValor(String clave, String valor) {
        Configuracion config = configuracionRepository.findById(clave)
                .orElse(Configuracion.builder().clave(clave).build());
        config.setValor(valor);
        configuracionRepository.save(config);
        log.info("Configuración actualizada: {} = {}", clave, valor);
    }

    @Override
    public Map<String, String> obtenerTodas() {
        Map<String, String> configs = new HashMap<>();
        configuracionRepository.findAll().forEach(c -> configs.put(c.getClave(), c.getValor()));
        return configs;
    }

    @Override
    @Transactional
    public void actualizarConfiguracion(Map<String, String> configuraciones) {
        configuraciones.forEach(this::setValor);
    }
}