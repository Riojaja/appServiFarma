package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.response.CajaResponse;
import com.example.proyecto.app.entity.Caja;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface CajaMapper {

    /**
     * Convierte una entidad Caja a CajaResponse.
     * Mapea los IDs y nombres de las entidades relacionadas (UsuarioApertura, UsuarioCierre).
     */
    @Mapping(source = "usuarioApertura.id", target = "usuarioAperturaId")
    @Mapping(source = "usuarioApertura.nombreCompleto", target = "usuarioAperturaNombre")
    @Mapping(source = "usuarioCierre.id", target = "usuarioCierreId")
    @Mapping(source = "usuarioCierre.nombreCompleto", target = "usuarioCierreNombre")
    CajaResponse toResponse(Caja entity);
}