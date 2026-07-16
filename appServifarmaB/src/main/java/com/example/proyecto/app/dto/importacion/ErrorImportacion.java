package com.example.proyecto.app.dto.importacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorImportacion {
    private int fila;
    private String mensaje;
    private String datos;
}