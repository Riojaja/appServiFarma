package com.example.proyecto.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponse {

    private Integer id;
    private String nombre;
    private String codigoBarras;
    private String principioActivo;
    private String imagen;
    private Boolean esGenerico;
    private BigDecimal precioVentaActual;
    private Integer stockMinimo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Datos de la categoría (mapeados desde Categoria en ProductoMapper)
    private Integer categoriaId;
    private String categoriaNombre;

    // Datos del fabricante (mapeados desde Fabricante en ProductoMapper)
    private Integer fabricanteId;
    private String fabricanteNombre;

    // Datos del producto genérico asociado (auto-relación)
    private Integer productoGenericoId;
    private String productoGenericoNombre;
}
