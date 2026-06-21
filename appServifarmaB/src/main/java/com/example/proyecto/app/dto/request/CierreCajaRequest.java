package com.example.proyecto.app.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CierreCajaRequest {

    @NotNull(message = "El ID del usuario que cierra la caja es obligatorio")
    private Integer usuarioCierreId;

    @NotNull(message = "El monto declarado es obligatorio")
    @Positive(message = "El monto declarado debe ser mayor a cero")
    private BigDecimal montoCierreDeclarado;
}
