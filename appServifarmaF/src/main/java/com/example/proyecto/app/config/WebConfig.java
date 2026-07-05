package com.example.proyecto.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.format.DateTimeFormatter;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura el formateo global de fechas para los parámetros de tipo
     * LocalDate y LocalDateTime en los endpoints REST.
     * 
     * Esto permite que los parámetros en la URL (ej. ?fecha=2025-06-18)
     * se conviertan automáticamente a LocalDate sin necesidad de anotaciones
     * @DateTimeFormat en cada controlador.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        
        // Formato para fechas (ISO-8601: yyyy-MM-dd)
        registrar.setDateFormatter(DateTimeFormatter.ISO_LOCAL_DATE);
        
        // Formato para fecha-hora (ISO-8601: yyyy-MM-dd'T'HH:mm:ss)
        registrar.setDateTimeFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        // Formato para horas (ISO-8601: HH:mm:ss)
        registrar.setTimeFormatter(DateTimeFormatter.ISO_LOCAL_TIME);
        
        registrar.registerFormatters(registry);
    }

    /**
     * Configura interceptores de peticiones HTTP.
     * Útil para logging, auditoría de peticiones, o inyección de datos comunes.
     * 
     * Ejemplo de uso:
     * - LogInterceptor: registra cada petición entrante.
     * - AuditInterceptor: registra quién hizo qué.
     * - TenancyInterceptor: para aplicaciones multi-tenant.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Los interceptores se añaden aquí si es necesario en el futuro.
        // Por ahora se deja vacío, pero la estructura está preparada.
        // Ejemplo: registry.addInterceptor(new LogInterceptor()).addPathPatterns("/api/**");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")  // URL de tu frontend Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
