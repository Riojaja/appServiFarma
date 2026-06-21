package com.example.proyecto.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandaInsatisfechaResponse {

    private Integer id;
    private String productoSolicitado;
    private LocalDateTime fecha;
    private String clienteDocumento;
    private LocalDateTime createdAt;

    // Datos del usuario que registró la demanda (mapeados desde Usuario en DemandaInsatisfechaMapper)
    private Integer usuarioId;
    private String usuarioNombre;
}
