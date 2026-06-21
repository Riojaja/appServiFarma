package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.response.DetalleVentaResponse;
import com.example.proyecto.app.entity.DetalleVenta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface DetalleVentaMapper {

    /**
     * Convierte una entidad DetalleVenta a DetalleVentaResponse.
     * Mapea los IDs de las entidades relacionadas (Venta, Lote) y el nombre del producto.
     */
    @Mapping(source = "venta.id", target = "ventaId")
    @Mapping(source = "lote.id", target = "loteId")
    @Mapping(source = "lote.producto.nombre", target = "productoNombre")
    DetalleVentaResponse toResponse(DetalleVenta entity);
}