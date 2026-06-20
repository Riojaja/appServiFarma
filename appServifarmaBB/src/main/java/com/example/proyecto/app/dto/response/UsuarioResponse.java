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
public class UsuarioResponse {

    private Integer id;
    private String nombreCompleto;
    private String usuario;
    private Boolean activo;
    
    // Datos del rol (mapeados desde Rol en UsuarioMapper)
    private Integer rolId;
    private String rolNombre;
    
    // Fechas de auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
