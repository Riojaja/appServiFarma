package com.example.proyecto.app.mapper;

import com.example.proyecto.app.dto.request.ClienteRequest;
import com.example.proyecto.app.dto.response.ClienteResponse;
import com.example.proyecto.app.entity.Cliente;
import com.example.proyecto.app.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ClienteMapper {

    /**
     * Convierte un ClienteRequest a una entidad Cliente.
     * Ignora el ID (autogenerado), la fecha de creación y la lista de ventas.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    Cliente toEntity(ClienteRequest request);

    /**
     * Convierte una entidad Cliente a ClienteResponse.
     * Mapea todos los campos de la entidad.
     */
    ClienteResponse toResponse(Cliente entity);

    /**
     * Actualiza una entidad Cliente existente con los datos del request.
     * Ignora el ID, la fecha de creación y la lista de ventas.
     * MapStruct actualiza solo los campos que no son null en el request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    void updateEntity(@MappingTarget Cliente entity, ClienteRequest request);
}