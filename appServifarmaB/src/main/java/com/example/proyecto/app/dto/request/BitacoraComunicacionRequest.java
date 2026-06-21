package com.example.proyecto.app.dto.request;

import com.example.proyecto.app.entity.BitacoraComunicacion;
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
public class BitacoraComunicacionRequest {

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 2000, message = "El mensaje no puede exceder los 2000 caracteres")
    private String mensaje;

    @NotNull(message = "El tipo de mensaje es obligatorio")
    private BitacoraComunicacion.Tipo tipo;

    @NotNull(message = "El ID del usuario que crea el mensaje es obligatorio")
    private Integer usuarioId;
}