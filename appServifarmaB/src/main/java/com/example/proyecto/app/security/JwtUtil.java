package com.example.proyecto.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:defaultSecretKey123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // Genera una clave HMAC-SHA256 a partir del secret (requiere mínimo 256 bits = 32 bytes)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ==============================
    // GENERACIÓN DE TOKENS (AHORA CON userId)
    // ==============================

    /**
     * Genera un token JWT incluyendo el ID del usuario en los claims.
     * @param username Nombre de usuario
     * @param userId ID del usuario (se guarda en el claim "id" como Integer)
     * @return Token JWT firmado
     */
    public String generateToken(String username, Integer userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)                 // Claims (incluye "id")
                .subject(subject)               // Sujeto (usuario)
                .issuedAt(now)                  // Fecha de emisión
                .expiration(expiryDate)         // Fecha de expiración
                .signWith(key)                  // Firma con clave secreta
                .compact();
    }

    // ==============================
    // VALIDACIÓN DE TOKENS
    // ==============================

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // ==============================
    // EXTRACCIÓN DE DATOS DEL TOKEN
    // ==============================

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el ID del usuario del token.
     * Si el claim "id" no existe o es nulo, devuelve null sin lanzar excepción.
     *
     * IMPORTANTE: el claim se guarda como Integer en generateToken()
     * (claims.put("id", userId)), así que debe leerse con Integer.class.
     * Antes se pedía claims.get("id", String.class), lo cual NO coincide con
     * el tipo real que JJWT tiene almacenado internamente — esto hacía que la
     * llamada lanzara una excepción SIEMPRE (capturada aquí abajo), devolviendo
     * null en cada request sin que se notara. Eso podía hacer fallar
     * silenciosamente la verificación de blacklist por usuario en
     * JwtAuthenticationFilter (usuarioId llegaba null todo el tiempo).
     */
    public Integer extractUserId(String token) {
        try {
            return extractClaim(token, claims -> claims.get("id", Integer.class));
        } catch (Exception e) {
            return null;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();  // En versiones modernas de JJWT se usa getPayload()
    }

    // ==============================
    // VERIFICACIONES DE ESTADO
    // ==============================

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration.before(new Date());
    }
}