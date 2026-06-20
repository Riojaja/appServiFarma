package com.example.proyecto.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")  // Aplica a todos los endpoints
                        .allowedOrigins("http://localhost:4200", "http://localhost:8080") // Orígenes permitidos
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Métodos HTTP permitidos
                        .allowedHeaders("*") // Todos los headers permitidos
                        .exposedHeaders("Authorization") // Headers que el frontend puede leer
                        .allowCredentials(true) // Permite enviar cookies o credenciales
                        .maxAge(3600); // Tiempo de cache de la preflight (en segundos)
            }
        };
    }
}
