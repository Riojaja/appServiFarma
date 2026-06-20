package com.example.proyecto.app.config;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Soporte para Java 8 Time
        mapper.registerModule(new JavaTimeModule());

        // 2. Formato ISO-8601 para fechas (no timestamps numéricos)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 3. Tolerancia a propiedades desconocidas
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 4. Exclusión de nulos (método actualizado)
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }
}