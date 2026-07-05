package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.request.FabricanteRequest;
import com.example.proyecto.app.dto.response.FabricanteResponse;
import com.example.proyecto.app.entity.Fabricante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface FabricanteMapper {

    /**
     * Convierte un FabricanteRequest a una entidad Fabricante.
     * Ignora el ID (autogenerado), la fecha de creación y la lista de productos.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "productos", ignore = true)
    Fabricante toEntity(FabricanteRequest request);

    /**
     * Convierte una entidad Fabricante a FabricanteResponse.
     * Mapea todos los campos de la entidad.
     */
    FabricanteResponse toResponse(Fabricante entity);

    /**
     * Actualiza una entidad Fabricante existente con los datos del request.
     * Ignora el ID, la fecha de creación y la lista de productos.
     * MapStruct actualiza solo los campos que no son null en el request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "productos", ignore = true)
    void updateEntity(@MappingTarget Fabricante entity, FabricanteRequest request);
}
