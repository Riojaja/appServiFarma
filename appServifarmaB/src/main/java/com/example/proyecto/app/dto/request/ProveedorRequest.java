package com.example.proyecto.app.dto.request;

import jakarta.validation.constraints.Email;
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
public class ProveedorRequest {

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 dígitos")
    private String ruc;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 150, message = "La razón social no puede exceder los 150 caracteres")
    private String razonSocial;

    @Size(max = 200, message = "La dirección no puede exceder los 200 caracteres")
    private String direccion;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;

    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
    private String email;

    @Size(max = 100, message = "El nombre de contacto no puede exceder los 100 caracteres")
    private String contacto;

    @Size(max = 50, message = "La región no puede exceder los 50 caracteres")
    private String region;
}