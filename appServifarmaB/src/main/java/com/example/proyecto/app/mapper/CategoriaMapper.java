package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.request.CategoriaRequest;
import com.example.proyecto.app.dto.response.CategoriaResponse;
import com.example.proyecto.app.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface CategoriaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "productos", ignore = true)
    Categoria toEntity(CategoriaRequest request);

    CategoriaResponse toResponse(Categoria entity);

    // Método para actualizar entidad existente con datos del request (ignorando nulls)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "productos", ignore = true)
    void updateEntity(@MappingTarget Categoria entity, CategoriaRequest request);
}
