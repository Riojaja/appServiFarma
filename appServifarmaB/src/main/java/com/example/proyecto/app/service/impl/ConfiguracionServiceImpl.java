package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.entity.Configuracion;
import com.example.proyecto.app.repository.ConfiguracionRepository;
import com.example.proyecto.app.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Override
    public List<LocalTime> getHorasCierreTurno() {
        String valor = getValor("horas_cierre_turno");
        if (valor == null || valor.trim().isEmpty())
            return Collections.emptyList();

        return Arrays.stream(valor.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::parsearHora)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private LocalTime parsearHora(String horaStr) {
    try {
        // Acepta "HH:MM" (ej: "15:30")
        return LocalTime.parse(horaStr);
    } catch (DateTimeParseException e) {
        // Si viene "15.30", convertirlo a "15:30"
        if (horaStr.contains(".")) {
            String corregida = horaStr.replace('.', ':');
            try {
                return LocalTime.parse(corregida);
            } catch (DateTimeParseException ex) {
                log.warn("Formato de hora inválido: {}", horaStr);
                return null;
            }
        }
        log.warn("Formato de hora inválido: {}", horaStr);
        return null;
    }
}
}