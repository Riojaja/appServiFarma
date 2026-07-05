package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.response.MovimientoStockResponse;
import com.example.proyecto.app.entity.MovimientoStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface MovimientoStockMapper {

    /**
     * Convierte una entidad MovimientoStock a MovimientoStockResponse.
     * Extrae los IDs y nombres de las entidades relacionadas (Lote, Usuario).
     */
    @Mapping(source = "lote.id", target = "loteId")
    @Mapping(source = "lote.producto.nombre", target = "nombreProducto")
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombreCompleto", target = "nombreUsuario")
    MovimientoStockResponse toResponse(MovimientoStock entity);
}
