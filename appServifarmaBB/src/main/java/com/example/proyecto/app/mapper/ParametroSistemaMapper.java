package com.example.proyecto.app.mapper;

import com.example.proyecto.app.dto.request.ParametroSistemaRequest;
import com.example.proyecto.app.dto.response.ParametroSistemaResponse;
import com.example.proyecto.app.entity.ParametroSistema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParametroSistemaMapper {

    /**
     * Convierte un ParametroSistemaRequest a una entidad ParametroSistema.
     * Ignora 'id', 'createdAt' y 'updatedAt' porque son autogenerados o auditables.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ParametroSistema toEntity(ParametroSistemaRequest request);

    /**
     * Convierte una entidad ParametroSistema a ParametroSistemaResponse.
     * Mapea todos los campos.
     */
    ParametroSistemaResponse toResponse(ParametroSistema entity);
}
