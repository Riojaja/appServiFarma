package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.BitacoraComunicacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BitacoraComunicacionResponse {

    private Integer id;
    private String mensaje;
    private LocalDateTime fechaHora;
    private BitacoraComunicacion.Tipo tipo;
    private Boolean leido;
    private LocalDateTime createdAt;

    // Datos del usuario que creó el mensaje (mapeados desde Usuario en BitacoraComunicacionMapper)
    private Integer usuarioId;
    private String usuarioNombre;
}