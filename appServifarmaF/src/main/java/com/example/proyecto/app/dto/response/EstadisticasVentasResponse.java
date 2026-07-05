package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.Venta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta para estadísticas avanzadas de ventas (RF13).
 * Contiene métricas de resumen, distribución por medios de pago,
 * ranking de productos más vendidos y tendencia diaria.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasVentasResponse {

    /**
     * Fecha y hora de inicio del período analizado.
     */
    private LocalDateTime fechaInicio;

    /**
     * Fecha y hora de fin del período analizado.
     */
    private LocalDateTime fechaFin;

    /**
     * Total de ventas (suma de totales) en el período.
     */
    private BigDecimal totalVentas;

    /**
     * Número total de transacciones (boletas/facturas) en el período.
     */
    private Long totalTransacciones;

    /**
     * Ticket promedio = totalVentas / totalTransacciones.
     */
    private BigDecimal ticketPromedio;

    /**
     * Distribución de ventas por medio de pago (efectivo, tarjeta, etc.).
     */
    private List<DistribucionPago> distribucionMediosPago;

    /**
     * Ranking de los productos más vendidos (por cantidad).
     */
    private List<ProductoTop> productosMasVendidos;

    /**
     * Tendencia diaria de ventas (agrupado por día).
     */
    private List<TendenciaDiaria> tendenciaDiaria;

    /**
     * Variación porcentual comparada con el período anterior.
     * Ejemplo: +15.50% o -2.30%.
     */
    private BigDecimal variacionPorcentual;

    // ==============================
    // CLASES ANIDADAS (ESTADÍSTICAS ESPECÍFICAS)
    // ==============================

    /**
     * Distribución de ventas por medio de pago.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistribucionPago {

        /**
         * Medio de pago (efectivo, tarjeta, transferencia, yape).
         */
        private Venta.MedioPago medioPago;

        /**
         * Total facturado para este medio de pago.
         */
        private BigDecimal total;

        /**
         * Cantidad de transacciones realizadas con este medio.
         */
        private Long cantidadTransacciones;
    }

    /**
     * Producto más vendido (top ranking).
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductoTop {

        /**
         * ID del producto.
         */
        private Integer productoId;

        /**
         * Nombre del producto.
         */
        private String productoNombre;

        /**
         * Cantidad total de unidades vendidas.
         */
        private Integer cantidadVendida;

        /**
         * Total facturado por este producto.
         */
        private BigDecimal totalFacturado;
    }

    /**
     * Tendencia diaria de ventas.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TendenciaDiaria {

        /**
         * Fecha del día.
         */
        private LocalDate fecha;

        /**
         * Total de ventas de ese día.
         */
        private BigDecimal total;
    }
}