package com.example.proyecto.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    /**
     * Bean para realizar peticiones HTTP a servicios externos.
     * Útil para integraciones futuras (ej. SUNAT, facturación electrónica, APIs de proveedores).
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);    // 5 segundos para conectar
        factory.setReadTimeout(10000);      // 10 segundos para leer la respuesta
        return new RestTemplate();
    }
}
