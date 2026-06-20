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
public class FabricanteResponse {

    private Integer id;
    private String nombre;
    private String contacto;
    private String telefono;
    private String email;
    private LocalDateTime createdAt;
}