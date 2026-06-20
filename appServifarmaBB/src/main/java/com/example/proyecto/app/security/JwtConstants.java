package com.example.proyecto.app.security;

public final class JwtConstants {

    private JwtConstants() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Prefijo del token JWT en el header Authorization.
     * Ejemplo: "Bearer " + token
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * Nombre del header HTTP donde se envía el token JWT.
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";

    /**
     * Tiempo de expiración por defecto en milisegundos (24 horas).
     * Se puede sobrescribir desde application.properties con 'jwt.expiration'.
     */
    public static final long DEFAULT_EXPIRATION = 86400000L; // 24 horas

    /**
     * Clave secreta por defecto (solo para desarrollo).
     * En producción se debe configurar con una clave segura en application.properties.
     */
    public static final String DEFAULT_SECRET = "defaultSecretKey123456789012345678901234567890";
}