package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.Caja;
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
public class CajaResponse {

    private Integer id;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoApertura;
    private BigDecimal montoCierreDeclarado;
    private Caja.EstadoCaja estado;
    private LocalDateTime createdAt;

    // Datos del usuario que abrió la caja (mapeados desde UsuarioApertura en CajaMapper)
    private Integer usuarioAperturaId;
    private String usuarioAperturaNombre;

    // Datos del usuario que cerró la caja (mapeados desde UsuarioCierre en CajaMapper)
    // Puede ser null si la caja aún está abierta
    private Integer usuarioCierreId;
    private String usuarioCierreNombre;
}