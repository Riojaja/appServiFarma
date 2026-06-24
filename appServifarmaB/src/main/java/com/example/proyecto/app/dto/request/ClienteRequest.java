package com.example.proyecto.app.dto.request;

import com.example.proyecto.app.entity.Cliente;
import jakarta.validation.constraints.Email;
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
public class ClienteRequest {

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @NotNull(message = "El tipo de documento es obligatorio")
    private Cliente.DocumentoTipo documentoTipo;

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no puede exceder los 20 caracteres")
    private String documentoNumero;

    @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
    private String telefono;

    @Size(max = 150, message = "La dirección no puede exceder los 150 caracteres")
    private String direccion;

    @Email(message = "El formato del correo electrónico no es válido")
    @Size(max = 100, message = "El correo no puede exceder los 100 caracteres")
    private String email;
}
