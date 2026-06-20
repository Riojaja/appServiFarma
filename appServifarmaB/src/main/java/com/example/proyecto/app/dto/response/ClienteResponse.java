package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.Cliente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {

    private Integer id;
    private String nombre;
    private Cliente.DocumentoTipo documentoTipo;
    private String documentoNumero;
    private String telefono;
    private String direccion;
    private String email;
    private LocalDateTime createdAt;
}