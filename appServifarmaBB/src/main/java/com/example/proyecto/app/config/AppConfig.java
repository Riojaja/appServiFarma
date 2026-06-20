package com.example.proyecto.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * Bean para realizar peticiones HTTP a servicios externos.
     * Útil para integraciones futuras (ej. SUNAT, facturación electrónica, APIs de proveedores).
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
