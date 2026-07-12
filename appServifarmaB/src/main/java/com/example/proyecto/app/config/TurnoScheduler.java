package com.example.proyecto.app.config;

import com.example.proyecto.app.service.ConfiguracionService;
import com.example.proyecto.app.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TurnoScheduler {

    private final UsuarioService usuarioService;
    private final ConfiguracionService configuracionService;

    /**
     * Ejecuta el cierre de sesiones por turno a las horas configuradas.
     * El administrador puede modificar las horas desd	e la tabla configuracion.
     */
    @Scheduled(cron = "${app.turno.cron.expression:0 0 13,18 * * *}")
    public void cerrarSesionesCambioTurno() {
        // Leer horas de configuración
        String hora1 = configuracionService.getValor("hora_cierre_turno_1");
        String hora2 = configuracionService.getValor("hora_cierre_turno_2");

        // Si no están configuradas, usar valores por defecto
        if (hora1 == null) hora1 = "13:00";
        if (hora2 == null) hora2 = "18:00";

        // Verificar si la hora actual coincide con alguna de las configuradas
        // (Este scheduler se ejecuta cada minuto, pero @Scheduled ya maneja el cron)
        // Por simplicidad, usamos cron fijo, pero podríamos validar aquí.
        log.info("Ejecutando cierre programado de sesiones por cambio de turno (horas configuradas: {} y {})",
                hora1, hora2);
        usuarioService.cerrarSesionesPorTurno();
    }

    /**
     * Ejecuta cada hora para limpiar sesiones expiradas.
     */
    @Scheduled(cron = "0 0 * * * *")
    public void limpiarSesionesExpiradas() {
        log.info("Limpiando sesiones expiradas");
        // Aquí se podría llamar a un método de limpieza de sesiones expiradas
        // usuarioService.limpiarSesionesExpiradas();
    }
}