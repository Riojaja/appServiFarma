package com.example.proyecto.app.dto.importacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImportDTO {
    private String codigoBarras;
    private String nombre;
    private String principioActivo;
    private String categoriaNombre;
    private String fabricanteNombre;
    private Double precioCompra;
    private Double precioVenta;
    private Integer stockMinimo;
    private Integer stockMaximo;
    private String unidadMedida;
    private String concentracion;
    private String presentacion;
    private Boolean esGenerico;
    private Boolean requiereReceta;
    private String imagenUrl;
    private Integer fila;
}