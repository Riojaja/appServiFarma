package com.example.proyecto.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración única y centralizada de CORS.
 *
 * IMPORTANTE: esta es la ÚNICA fuente de configuración de CORS del proyecto.
 * Antes existían dos configuraciones distintas (esta clase y WebConfig),
 * que se solapaban y podían dar comportamientos inconsistentes según el
 * orden de carga de los beans. WebConfig ya NO define CORS.
 *
 * Los orígenes se leen desde application.properties / application-prod.properties
 * (app.cors.allowed-origins), así que cambiar el dominio del frontend en
 * producción no requiere tocar código, solo la variable de entorno correspondiente.
 */
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        if ("*".equals(allowedHeaders.trim())) {
            configuration.setAllowedHeaders(List.of("*"));
        } else {
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        }

        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}