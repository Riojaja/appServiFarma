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
public class ParametroSistemaResponse {

    private Integer id;
    private String clave;
    private String valor;
    private String descripcion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}