package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.request.ProveedorRequest;
import com.example.proyecto.app.dto.response.ProveedorResponse;
import com.example.proyecto.app.entity.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProveedorMapper {

    /**
     * Convierte un ProveedorRequest a una entidad Proveedor.
     * Ignora el ID (autogenerado), la fecha de creación y la lista de lotes.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lotes", ignore = true)
    Proveedor toEntity(ProveedorRequest request);

    /**
     * Convierte una entidad Proveedor a ProveedorResponse.
     * Mapea todos los campos de la entidad.
     */
    ProveedorResponse toResponse(Proveedor entity);

    /**
     * Actualiza una entidad Proveedor existente con los datos del request.
     * Ignora el ID, la fecha de creación y la lista de lotes.
     * MapStruct actualiza solo los campos que no son null en el request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lotes", ignore = true)
    void updateEntity(@MappingTarget Proveedor entity, ProveedorRequest request);
}