package com.example.proyecto.app.dto.importacion;

import com.example.proyecto.app.dto.response.ProductoResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoImportacion {
    private int totalFilas;
    private int importados;
    private int errores;
    private List<ErrorImportacion> erroresDetalle;
    private List<ProductoResponse> productosImportados;
}