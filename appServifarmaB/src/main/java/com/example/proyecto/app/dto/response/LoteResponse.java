package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.Lote;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteResponse {

    private Integer id;
    private String lote;
    private LocalDate fechaIngreso;
    private LocalDate fechaVencimiento;
    private Integer cantidad;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Lote.EstadoLote estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Datos del producto (mapeados desde Producto en LoteMapper)
    private Integer productoId;
    private String productoNombre;

    // Datos del proveedor (mapeados desde Proveedor en LoteMapper)
    private Integer proveedorId;
    private String proveedorRazonSocial;
}