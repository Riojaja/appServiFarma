package com.example.proyecto.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configura un pool de hilos para tareas asíncronas.
     * Útil para operaciones que no deben bloquear la respuesta de la API,
     * como la generación de reportes en Excel/PDF, envío de correos,
     * procesamiento de facturas electrónicas, etc.
     *
     * @return Executor configurado con tamaño de pool y cola de espera
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);                 // Hilos mínimos siempre disponibles
        executor.setMaxPoolSize(10);                 // Máximo de hilos activos
        executor.setQueueCapacity(100);              // Capacidad de la cola de espera
        executor.setKeepAliveSeconds(60);            // Tiempo de inactividad antes de liberar hilos extra
        executor.setThreadNamePrefix("ServiFarma-Async-"); // Nombre descriptivo para los hilos
        executor.initialize();
        return executor;
    }
}
