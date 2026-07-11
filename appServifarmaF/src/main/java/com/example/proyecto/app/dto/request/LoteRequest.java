package com.example.proyecto.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoteRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Integer proveedorId;

    @NotBlank(message = "El número de lote es obligatorio")
    @Size(max = 50, message = "El número de lote no puede exceder los 50 caracteres")
    private String lote;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    // NOTA: se quitó @Future de aquí porque este DTO se reutiliza en actualizarLote().
    // La validación de "debe ser futura" ahora se hace SOLO en LoteServiceImpl.crearLote(),
    // así se puede editar un lote ya vencido sin que la validación lo rechace.
    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El precio de compra es obligatorio")
    @Positive(message = "El precio de compra debe ser mayor a cero")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a cero")
    private BigDecimal precioVenta;
    
    private Integer usuarioId;
}