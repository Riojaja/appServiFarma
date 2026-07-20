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
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/uploads/**").permitAll() // Imágenes públicas

                        // 🔥 NUEVO: Permitir Health Check y raíz (para Railway)
                        .requestMatchers("/", "/actuator/health", "/actuator/**").permitAll()
                        // 🔥 Fin de la modificación

                        // ⚠️ IMPORTANTE: se cambió hasAuthority("ADMIN") por hasRole("ADMIN").
                        // El proyecto ya usa @PreAuthorize("hasRole('ADMIN')") en varios
                        // controllers (ej. ProductoController). hasRole() internamente busca
                        // la autoridad "ROLE_ADMIN", mientras que hasAuthority("ADMIN") busca
                        // literalmente "ADMIN" sin prefijo. Tener las DOS convenciones mezcladas
                        // en el mismo proyecto hace que una de las dos falle siempre con 403,
                        // dependiendo del formato real que entregue tu UserDetailsService.
                        // Se unifica todo en hasRole(...) porque es la convención que ya
                        // predomina en los @PreAuthorize existentes.
                        //
                        // ⚠️ Esto requiere que tu UserDetailsService le dé a cada usuario la
                        // autoridad como "ROLE_ADMIN" / "ROLE_VENDEDOR" (con el prefijo
                        // "ROLE_"). Si en tu UserDetailsService actual las autoridades se
                        // arman como "ADMIN" (sin prefijo), hay que agregarle el prefijo ahí
                        // — revisa ese archivo (ver nota al final de la respuesta).
                        .requestMatchers("/api/parametros/**").hasRole("ADMIN")
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                        // Cualquier otra petición requiere autenticación
                        .anyRequest().authenticated()
                )
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