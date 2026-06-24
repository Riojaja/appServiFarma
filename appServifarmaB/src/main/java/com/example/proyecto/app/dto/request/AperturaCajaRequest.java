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
public class AperturaCajaRequest {

    @NotNull(message = "El ID del usuario que abre la caja es obligatorio")
    private Integer usuarioAperturaId;

    @NotNull(message = "El monto de apertura es obligatorio")
    @Positive(message = "El monto de apertura debe ser mayor a cero")
    private BigDecimal montoApertura;
}