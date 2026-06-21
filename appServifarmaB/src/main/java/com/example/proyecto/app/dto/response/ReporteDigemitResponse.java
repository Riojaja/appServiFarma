package com.example.proyecto.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para el reporte mensual exigido por DIGEMIT (RF5, RF26).
 * Contiene la lista de todos los productos con sus lotes, vencimientos y cantidades.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDigemitResponse {

    /**
     * Mes del reporte en formato "YYYY-MM".
     */
    private String mes;

    /**
     * Fecha en que se generó el reporte.
     */
    private LocalDate fechaGeneracion;

    /**
     * Lista de items del reporte (productos y lotes).
     */
    private List<Item> items;

    /**
     * Item individual del reporte DIGEMIT.
     * Representa un producto con un lote específico.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {

        /**
         * Código de barras del producto (o "N/A" si no tiene).
         */
        private String codigoProducto;

        /**
         * Nombre comercial del producto.
         */
        private String nombreProducto;

        /**
         * Nombre del laboratorio/fabricante (o "N/A" si no tiene).
         */
        private String laboratorio;

        /**
         * Principio activo del medicamento (o "N/A" si no tiene).
         */
        private String principioActivo;

        /**
         * Número del lote.
         */
        private String lote;

        /**
         * Fecha de vencimiento del lote.
         */
        private LocalDate fechaVencimiento;

        /**
         * Cantidad disponible en el lote (stock actual o 0 si está agotado).
         */
        private Integer cantidad;
    }
}