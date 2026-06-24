package com.example.proyecto.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaResponse {

    private Integer id;
    private Integer cantidad;
    private BigDecimal precioUnitarioVenta;
    private BigDecimal precioCompraUnitario;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;

    // Datos de la venta (relación)
    private Integer ventaId;

    // Datos del lote y producto (relación)
    private Integer loteId;
    private String productoNombre;
}