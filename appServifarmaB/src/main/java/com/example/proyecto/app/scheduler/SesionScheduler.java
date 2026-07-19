package com.example.proyecto.app.scheduler;

import com.example.proyecto.app.repository.SesionUsuarioRepository;
import com.example.proyecto.app.service.ConfiguracionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SesionScheduler {

    private final SesionUsuarioRepository sesionRepository;
    private final ConfiguracionService configService;

    @Scheduled(cron = "0 * * * * ?") // Cada minuto
    @Transactional
    public void cerrarSesionesInactivas() {
        Integer minutos = configService.getValorInteger("tiempo_inactividad_minutos");
        if (minutos == null || minutos <= 0)
            return;
        LocalDateTime limite = LocalDateTime.now().minusMinutes(minutos);
        // ✅ Si el método devuelve void, no asignamos nada
        sesionRepository.cerrarSesionesInactivas(limite);
        log.info("🕒 Sesiones inactivas cerradas (límite: {} min)", minutos);
    }

    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void cerrarSesionesPorTurno() {
        List<LocalTime> horasCierre = configService.getHorasCierreTurno();
        if (horasCierre.isEmpty())
            return;

        LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);
        LocalTime horaActual = LocalTime.of(ahora.getHour(), ahora.getMinute());

        if (horasCierre.contains(horaActual)) {
            sesionRepository.cerrarSesionesDeVendedores();
            log.info("🔒 Cierre de turno ejecutado a las {}", horaActual);
        }
    }
}