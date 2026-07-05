package com.example.proyecto.app.dto.request;

import com.example.proyecto.app.entity.MovimientoStock;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AjusteStockRequest {

    @NotNull(message = "El ID del usuario que realiza el ajuste es obligatorio")
    private Integer usuarioId;

    // Cantidad SIEMPRE positiva: el signo lo decide tipoMovimiento
    // (ajuste = aumenta, merma = reduce). El controller la convierte internamente.
    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovimientoStock.TipoMovimiento tipoMovimiento;

    @NotBlank(message = "La observación es obligatoria para auditar el ajuste")
    @Size(max = 255, message = "La observación no puede exceder los 255 caracteres")
    private String observacion;
}