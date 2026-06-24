package com.example.proyecto.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandaInsatisfechaRequest {

    @NotBlank(message = "El nombre del producto solicitado es obligatorio")
    @Size(max = 200, message = "El nombre del producto no puede exceder los 200 caracteres")
    private String productoSolicitado;

    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    private String clienteDocumento; // Opcional

    @NotNull(message = "El ID del usuario que registra la demanda es obligatorio")
    private Integer usuarioId;
}