package com.example.proyecto.app.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
// IMPORTANTE: sin esta anotación, Spring IGNORA EN SILENCIO todos los
// @PreAuthorize/@PostAuthorize del proyecto (Auditoria, Estadistica, Reporte).
// Antes de este fix, cualquier usuario autenticado (incluido "vendedor")
// podía acceder a esos endpoints marcados como "solo ADMIN".
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    // Inyectado desde config/CorsConfig.java: única fuente de verdad para CORS.
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF ya que usamos JWT (stateless)
                .csrf(csrf -> csrf.disable())

                // CORS centralizado (ver config/CorsConfig.java)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Configurar manejo de excepciones de autenticación
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Política de sesión sin estado (stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (autenticación)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Documentación Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Endpoints de parámetros del sistema (solo admin)
                        .requestMatchers("/api/parametros/**").hasRole("ADMIN")
                        // Endpoints de usuarios (solo admin)
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // Los demás endpoints requieren autenticación;
                        // las restricciones más finas (ej. solo ADMIN puede
                        // eliminar catálogos) se hacen con @PreAuthorize
                        // en el controlador correspondiente.
                        .anyRequest().authenticated()
                )
                // Añadir el filtro JWT antes del filtro de autenticación por defecto
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}