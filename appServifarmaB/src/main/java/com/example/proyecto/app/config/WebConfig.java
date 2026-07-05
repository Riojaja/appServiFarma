package com.example.proyecto.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

/**
 * NOTA: este archivo ya NO configura CORS.
 * Antes tenía su propio addCorsMappings() que chocaba con CorsConfig.java
 * (distintos orígenes y métodos permitidos según el archivo). Ahora existe
 * una única fuente de verdad para CORS: config/CorsConfig.java, usada por
 * SecurityConfig. Ver ese archivo para cambiar orígenes permitidos.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura el formateo global de fechas para los parámetros de tipo
     * LocalDate y LocalDateTime en los endpoints REST.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();

        registrar.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        registrar.setDateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        registrar.setTimeFormatter(DateTimeFormatter.ISO_LOCAL_TIME);

        registrar.registerFormatters(registry);
    }

    /**
     * Configura interceptores de peticiones HTTP.
     * Por ahora vacío, la estructura está preparada para el futuro.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Ejemplo: registry.addInterceptor(new LogInterceptor()).addPathPatterns("/api/**");
    }
}