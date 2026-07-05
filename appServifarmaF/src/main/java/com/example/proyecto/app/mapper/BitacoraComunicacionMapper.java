package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.response.BitacoraComunicacionResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface BitacoraComunicacionMapper {

    /**
     * Convierte una entidad BitacoraComunicacion a BitacoraComunicacionResponse.
     * Mapea los IDs y nombres de las entidades relacionadas (Usuario).
     */
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombreCompleto", target = "usuarioNombre")
    BitacoraComunicacionResponse toResponse(BitacoraComunicacion entity);
}