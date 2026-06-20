package com.example.proyecto.app.mapper;

import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.UsuarioResponse;
import com.example.proyecto.app.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ventas", ignore = true)
    @Mapping(target = "cajasApertura", ignore = true)
    @Mapping(target = "cajasCierre", ignore = true)
    @Mapping(target = "movimientosStock", ignore = true)
    @Mapping(target = "demandasInsatisfechas", ignore = true)
    @Mapping(target = "bitacoraComunicaciones", ignore = true)
    Usuario toEntity(UsuarioRequest request);

    @Mapping(source = "rol.id", target = "rolId")
    @Mapping(source = "rol.nombre", target = "rolNombre")
    UsuarioResponse toResponse(Usuario usuario);

    void updateEntity(UsuarioRequest request, @MappingTarget Usuario entity);
}
