package com.example.proyecto.app.dto.request;

import com.example.proyecto.app.entity.MovimientoStock;
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

    @NotNull(message = "El ID del lote es obligatorio")
    private Integer loteId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovimientoStock.TipoMovimiento tipoMovimiento;

    @Size(max = 255, message = "La observación no puede exceder los 255 caracteres")
    private String observacion;

}
