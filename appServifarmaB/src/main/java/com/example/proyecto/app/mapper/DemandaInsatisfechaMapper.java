package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.response.DemandaInsatisfechaResponse;
import com.example.proyecto.app.entity.DemandaInsatisfecha;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface DemandaInsatisfechaMapper {

    /**
     * Convierte una entidad DemandaInsatisfecha a DemandaInsatisfechaResponse.
     * Mapea los IDs y nombres de las entidades relacionadas (Usuario).
     */
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombreCompleto", target = "usuarioNombre")
    DemandaInsatisfechaResponse toResponse(DemandaInsatisfecha entity);
}