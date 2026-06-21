package com.example.proyecto.app.config;

import com.example.proyecto.app.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Configuración de tareas programadas del sistema.
 * Implementa los requisitos RF6 y RF8: Alertas automáticas de stock y vencimiento.
 * 
 * @author ServiFarma Development Team
 * @since 1.0
 */
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final InventarioService inventarioService;

    /**
     * Actualiza el estado de los lotes vencidos automáticamente.
     * Se ejecuta diariamente a la 1:00 AM.
     * 
     * Requisito: RF2 - Gestión de lotes con control automático de caducidad.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void actualizarLotesVencidos() {
        try {
            log.info("Iniciando tarea programada: Actualización de lotes vencidos");
            int lotesActualizados = inventarioService.actualizarEstadoLotesVencidos();
            log.info("Tarea completada: {} lotes marcados como vencidos", lotesActualizados);
        } catch (Exception e) {
            log.error("Error al ejecutar tarea de actualización de lotes vencidos", e);
        }
    }

    /**
     * Verifica y registra alertas de productos con stock bajo.
     * Se ejecuta diariamente a las 8:00 AM (inicio del turno de mañana).
     * 
     * Requisito: RF8 - Alertas de stock mínimo para prevenir desabastecimiento.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void verificarStockBajo() {
        try {
            log.info("Iniciando tarea programada: Verificación de stock bajo");
            var productosConStockBajo = inventarioService.obtenerProductosConStockBajo();
            
            if (!productosConStockBajo.isEmpty()) {
                log.warn("ALERTA: {} productos con stock por debajo del mínimo", productosConStockBajo.size());
                // Aquí se podría integrar con un sistema de notificaciones (email, SMS, etc.)
                productosConStockBajo.forEach(productoId -> 
                    log.warn("  - Producto ID {} requiere reposición", productoId)
                );
            } else {
                log.info("Verificación completada: Todos los productos tienen stock suficiente");
            }
        } catch (Exception e) {
            log.error("Error al ejecutar tarea de verificación de stock bajo", e);
        }
    }

    /**
     * Verifica y registra alertas de lotes próximos a vencer.
     * Se ejecuta diariamente a las 8:00 AM (inicio del turno de mañana).
     * 
     * Requisito: RF6 - Alertas de caducidad próxima con anticipación configurable.
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void verificarLotesProximosAVencer() {
        try {
            log.info("Iniciando tarea programada: Verificación de lotes próximos a vencer");
            
            var lotesProximosAVencer = inventarioService.obtenerLotesProximosAVencer();
            
            if (!lotesProximosAVencer.isEmpty()) {
                log.warn("ALERTA: {} lotes próximos a vencer", lotesProximosAVencer.size());
                // Aquí se podría integrar con un sistema de notificaciones
                lotesProximosAVencer.forEach(lote -> 
                    log.warn("  - Lote {}: Producto {} (ID: {}) vence el {}", 
                        lote.getLote(), 
                        lote.getProducto().getNombre(),
                        lote.getProducto().getId(),
                        lote.getFechaVencimiento())
                );
            } else {
                log.info("Verificación completada: No hay lotes próximos a vencer");
            }
        } catch (Exception e) {
            log.error("Error al ejecutar tarea de verificación de lotes próximos a vencer", e);
        }
    }

    /**
     * Actualiza automáticamente el estado de lotes agotados (cantidad = 0).
     * Se ejecuta cada hora durante el horario laboral.
     * 
     * Requisito: RF2 - Gestión automática de estado de lotes.
     */
    @Scheduled(cron = "0 0 8-20 * * ?") // Cada hora de 8 AM a 8 PM
    public void actualizarLotesAgotados() {
        try {
            log.debug("Verificando lotes agotados...");
            int lotesActualizados = inventarioService.actualizarEstadoLotesAgotados();
            
            if (lotesActualizados > 0) {
                log.info("Se actualizaron {} lotes a estado 'agotado'", lotesActualizados);
            }
        } catch (Exception e) {
            log.error("Error al ejecutar tarea de actualización de lotes agotados", e);
        }
    }
}
