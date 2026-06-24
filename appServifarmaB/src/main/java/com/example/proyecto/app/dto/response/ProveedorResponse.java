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
public class ProveedorResponse {

    private Integer id;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private String telefono;
    private String email;
    private String contacto;
    private String region;
    private LocalDateTime createdAt;
}