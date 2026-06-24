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
public class CategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 80, message = "El nombre no puede exceder los 80 caracteres")
    private String nombre;

    @Size(max = 200, message = "La descripción no puede exceder los 200 caracteres")
    private String descripcion;
}