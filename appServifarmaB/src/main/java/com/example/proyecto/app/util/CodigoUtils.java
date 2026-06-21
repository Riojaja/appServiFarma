package com.example.proyecto.app.util;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utilidad para generación de códigos de autorización, números de operación,
 * códigos de barras y otros identificadores alfanuméricos.
 */
public final class CodigoUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    // Caracteres permitidos para códigos alfanuméricos (excluye caracteres ambiguos)
    private static final String ALPHANUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private CodigoUtils() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Genera un código de autorización aleatorio para transacciones (ej. YAPE, transferencias).
     * Formato: 8 caracteres alfanuméricos en mayúsculas.
     *
     * @return Código de autorización de 8 caracteres
     */
    public static String generarCodigoAutorizacion() {
        return generarCodigoAlfanumerico(8);
    }

    /**
     * Genera un código alfanumérico aleatorio de la longitud especificada.
     *
     * @param longitud Número de caracteres (debe ser > 0)
     * @return Código alfanumérico en mayúsculas
     * @throws IllegalArgumentException Si la longitud es <= 0
     */
    public static String generarCodigoAlfanumerico(int longitud) {
        if (longitud <= 0) {
            throw new IllegalArgumentException("La longitud debe ser mayor a cero");
        }
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Genera un código numérico aleatorio de la longitud especificada.
     *
     * @param longitud Número de dígitos (debe ser > 0)
     * @return Código numérico (String de dígitos)
     */
    public static String generarCodigoNumerico(int longitud) {
        if (longitud <= 0) {
            throw new IllegalArgumentException("La longitud debe ser mayor a cero");
        }
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int digit = RANDOM.nextInt(10);
            sb.append(digit);
        }
        return sb.toString();
    }

    /**
     * Genera un UUID aleatorio (versión 4) como String.
     * Útil para identificadores únicos universales.
     *
     * @return UUID en formato estándar (ej. "550e8400-e29b-41d4-a716-446655440000")
     */
    public static String generarUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Genera un código de barras ficticio de 13 dígitos (formato EAN-13 simplificado).
     * No valida el dígito de control, solo genera un número aleatorio de 13 dígitos.
     *
     * @return Código de barras de 13 dígitos
     */
    public static String generarCodigoBarras() {
        return generarCodigoNumerico(13);
    }

    /**
     * Genera un código de lote ficticio para pruebas.
     * Formato: "L" + año + mes + día + 4 dígitos aleatorios.
     *
     * @return Código de lote (ej. "L202506201234")
     */
    public static String generarCodigoLote() {
        java.time.LocalDate now = java.time.LocalDate.now();
        String fecha = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String aleatorio = generarCodigoNumerico(4);
        return "L" + fecha + aleatorio;
    }

    /**
     * Valida que un código de autorización tenga el formato esperado (8 caracteres alfanuméricos).
     *
     * @param codigo Código a validar
     * @return true si es válido, false en caso contrario
     */
    public static boolean validarCodigoAutorizacion(String codigo) {
        if (codigo == null || codigo.length() != 8) {
            return false;
        }
        return codigo.matches("^[A-Z0-9]{8}$");
    }
}