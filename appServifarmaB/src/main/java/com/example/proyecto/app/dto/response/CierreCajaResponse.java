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
public class CierreCajaResponse {

    private Integer cajaId;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoApertura;
    private BigDecimal totalVentas;
    private BigDecimal montoDeclarado;
    private BigDecimal diferencia;
    private String usuarioApertura;
    private String usuarioCierre;
}