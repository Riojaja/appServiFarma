package com.example.proyecto.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequest {

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres")
    private String nombre;

    @Size(max = 50, message = "El código de barras no puede exceder los 50 caracteres")
    private String codigoBarras;

    @Size(max = 150, message = "El principio activo no puede exceder los 150 caracteres")
    private String principioActivo;

    @Size(max = 255, message = "La ruta de la imagen no puede exceder los 255 caracteres")
    private String imagen;

    @Builder.Default
    private Boolean esGenerico = false;

    @NotNull(message = "El precio de venta actual es obligatorio")
    @Positive(message = "El precio de venta debe ser mayor a cero")
    private BigDecimal precioVentaActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Positive(message = "El stock mínimo debe ser mayor a cero")
    @Builder.Default
    private Integer stockMinimo = 5;

    // Relaciones (IDs de las entidades relacionadas, pueden ser nulos)
    private Integer categoriaId;
    private Integer fabricanteId;
    private Integer productoGenericoId;
}
