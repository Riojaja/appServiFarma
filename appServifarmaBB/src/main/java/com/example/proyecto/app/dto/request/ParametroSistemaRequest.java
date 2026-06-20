package com.example.proyecto.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametroSistemaRequest {

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 50, message = "La clave no puede exceder los 50 caracteres")
    private String clave;

    @NotBlank(message = "El valor es obligatorio")
    @Size(max = 100, message = "El valor no puede exceder los 100 caracteres")
    private String valor;

    @Size(max = 200, message = "La descripción no puede exceder los 200 caracteres")
    private String descripcion;
}