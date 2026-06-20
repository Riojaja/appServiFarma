package com.example.proyecto.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Clase de utilidad para operaciones con fechas.
 * Proporciona métodos estáticos para cálculos de vencimiento,
 * días restantes, validaciones y formateo.
 */
public final class FechaUtils {

    private FechaUtils() {
        // Constructor privado para evitar instanciación
    }

    /**
     * Calcula el número de días entre la fecha actual y la fecha de vencimiento.
     * Si la fecha de vencimiento es anterior a hoy, devuelve un número negativo.
     *
     * @param fechaVencimiento Fecha de vencimiento (no nula)
     * @return Días restantes (puede ser negativo si ya venció)
     */
    public static long calcularDiasRestantes(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser nula");
        }
        return LocalDate.now().until(fechaVencimiento, ChronoUnit.DAYS);
    }

    /**
     * Verifica si una fecha de vencimiento ya pasó (es anterior a hoy).
     *
     * @param fechaVencimiento Fecha de vencimiento (no nula)
     * @return true si la fecha es anterior a hoy, false en caso contrario
     */
    public static boolean estaVencido(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser nula");
        }
        return fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Verifica si una fecha es válida (no nula y no es una fecha futura).
     * Útil para validar fechas de ingreso de lotes.
     *
     * @param fecha Fecha a validar (puede ser nula)
     * @return true si la fecha no es nula y no es futura
     */
    public static boolean esFechaValidaNoFutura(LocalDate fecha) {
        return fecha != null && !fecha.isAfter(LocalDate.now());
    }

    /**
     * Calcula la diferencia en días entre dos fechas.
     *
     * @param inicio Fecha de inicio (no nula)
     * @param fin    Fecha de fin (no nula)
     * @return Días entre ambas fechas (positivo si fin es posterior)
     */
    public static long diasEntre(LocalDate inicio, LocalDate fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        return inicio.until(fin, ChronoUnit.DAYS);
    }

    /**
     * Formatea una fecha LocalDate a un String con el patrón dado.
     * Si la fecha es nula, retorna una cadena vacía.
     *
     * @param fecha   Fecha a formatear
     * @param patron  Patrón de formato (ej. "dd/MM/yyyy")
     * @return Fecha formateada, o cadena vacía si fecha es nula
     */
    public static String formatearFecha(LocalDate fecha, String patron) {
        if (fecha == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patron);
        return fecha.format(formatter);
    }

    /**
     * Formatea una fecha LocalDate a String con formato ISO (yyyy-MM-dd).
     *
     * @param fecha Fecha a formatear
     * @return Fecha en formato ISO, o cadena vacía si es nula
     */
    public static String formatearFechaISO(LocalDate fecha) {
        return formatearFecha(fecha, "yyyy-MM-dd");
    }

    /**
     * Convierte un String a LocalDate usando el patrón dado.
     *
     * @param fechaStr String con la fecha
     * @param patron   Patrón de formato (ej. "dd/MM/yyyy")
     * @return LocalDate parseado, o null si el String es nulo o vacío
     * @throws java.time.format.DateTimeParseException Si el formato es inválido
     */
    public static LocalDate parsearFecha(String fechaStr, String patron) {
        if (fechaStr == null || fechaStr.isBlank()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(patron);
        return LocalDate.parse(fechaStr, formatter);
    }

    /**
     * Convierte un String a LocalDate usando el formato ISO (yyyy-MM-dd).
     *
     * @param fechaStr String con la fecha en formato ISO
     * @return LocalDate parseado, o null si el String es nulo o vacío
     */
    public static LocalDate parsearFechaISO(String fechaStr) {
        return parsearFecha(fechaStr, "yyyy-MM-dd");
    }

    /**
     * Obtiene la fecha actual como LocalDate.
     *
     * @return Fecha actual
     */
    public static LocalDate fechaActual() {
        return LocalDate.now();
    }

    /**
     * Obtiene la fecha y hora actual como LocalDateTime.
     *
     * @return Fecha y hora actual
     */
    public static LocalDateTime fechaHoraActual() {
        return LocalDateTime.now();
    }

    /**
     * Verifica si una fecha está dentro de un rango (inclusive).
     *
     * @param fecha Fecha a verificar
     * @param inicio Fecha de inicio del rango
     * @param fin    Fecha de fin del rango
     * @return true si la fecha está entre inicio y fin (inclusive)
     */
    public static boolean estaEnRango(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        if (fecha == null || inicio == null || fin == null) {
            return false;
        }
        return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    /**
     * Obtiene la fecha de vencimiento ajustada según un número de días de anticipación.
     * Útil para configurar alertas: fechaLimite = fechaVencimiento - diasAnticipacion.
     *
     * @param fechaVencimiento Fecha de vencimiento
     * @param diasAnticipacion Días de anticipación (positivo)
     * @return Fecha límite para la alerta
     */
    public static LocalDate fechaLimiteAlerta(LocalDate fechaVencimiento, int diasAnticipacion) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser nula");
        }
        if (diasAnticipacion < 0) {
            throw new IllegalArgumentException("Los días de anticipación deben ser positivos");
        }
        return fechaVencimiento.minusDays(diasAnticipacion);
    }
}
