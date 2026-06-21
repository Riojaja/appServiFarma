package com.example.proyecto.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO de respuesta para el reporte de rentabilidad real (RF11).
 * Calcula el margen de ganancia neto descontando mermas (productos vencidos/deteriorados)
 * y los costos de compra de los productos vendidos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRentabilidadResponse {

    /**
     * Fecha de inicio del período analizado.
     */
    private LocalDate fechaInicio;

    /**
     * Fecha de fin del período analizado.
     */
    private LocalDate fechaFin;

    /**
     * Ingresos totales por ventas completadas en el período.
     */
    private BigDecimal ingresosTotales;

    /**
     * Costo total de compra de los productos vendidos (precio_compra_unitario * cantidad).
     */
    private BigDecimal costoVentas;

    /**
     * Total de mermas (productos caducados o deteriorados) en el período,
     * calculado como precio_compra * cantidad perdida.
     */
    private BigDecimal mermas;

    /**
     * Margen bruto = ingresosTotales - costoVentas.
     */
    private BigDecimal margenBruto;

    /**
     * Margen neto = margenBruto - mermas.
     * Refleja la rentabilidad real del negocio.
     */
    private BigDecimal margenNeto;

    /**
     * Desglose de rentabilidad por categoría de producto (opcional).
     */
    private List<RentabilidadCategoria> categorias;

    /**
     * Rentabilidad desglosada por categoría de producto.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RentabilidadCategoria {

        /**
         * Nombre de la categoría.
         */
        private String categoriaNombre;

        /**
         * Ingresos generados por productos de esta categoría.
         */
        private BigDecimal ingresos;

        /**
         * Costo de compra de los productos de esta categoría.
         */
        private BigDecimal costos;

        /**
         * Margen = ingresos - costos.
         */
        private BigDecimal margen;
    }
}