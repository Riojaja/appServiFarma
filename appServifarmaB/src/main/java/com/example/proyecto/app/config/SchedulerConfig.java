package com.example.proyecto.app.config;

import com.example.proyecto.app.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SchedulerConfig {

    private final UsuarioService usuarioService;

    /**
     * Ejecuta cierre de sesiones a las 1:00 PM y 6:00 PM (cambio de turno).
     */
    @Scheduled(cron = "0 0 13,18 * * *")
    public void cerrarSesionesCambioTurno() {
        log.info("Ejecutando cierre automático de sesiones por cambio de turno");
        try {
            usuarioService.cerrarSesionesPorTurno();
        } catch (Exception e) {
            log.error("Error al cerrar sesiones por cambio de turno: {}", e.getMessage());
        }
    }

    /**
     * Limpieza de tokens inválidos (opcional, cada 24 horas).
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void limpiarTokens() {
        log.info("Limpiando tokens inválidos antiguos");
        // Implementar limpieza si es necesario
    }
}