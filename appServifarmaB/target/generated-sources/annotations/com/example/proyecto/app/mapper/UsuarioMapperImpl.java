package com.example.proyecto.app.mapper;

import com.example.proyecto.app.dto.request.UsuarioRequest;
import com.example.proyecto.app.dto.response.UsuarioResponse;
import com.example.proyecto.app.entity.Rol;
import com.example.proyecto.app.entity.Usuario;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-20T01:16:16-0500",
    comments = "version: 1.6.0, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 25.0.1 (Eclipse Adoptium)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public Usuario toEntity(UsuarioRequest request) {
        if ( request == null ) {
            return null;
        }

        Usuario.UsuarioBuilder usuario = Usuario.builder();

        usuario.activo( request.getActivo() );
        usuario.contrasena( request.getContrasena() );
        usuario.nombreCompleto( request.getNombreCompleto() );
        usuario.usuario( request.getUsuario() );

        return usuario.build();
    }

    @Override
    public UsuarioResponse toResponse(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        UsuarioResponse.UsuarioResponseBuilder usuarioResponse = UsuarioResponse.builder();

        usuarioResponse.rolId( usuarioRolId( usuario ) );
        usuarioResponse.rolNombre( usuarioRolNombre( usuario ) );
        usuarioResponse.activo( usuario.getActivo() );
        usuarioResponse.createdAt( usuario.getCreatedAt() );
        usuarioResponse.id( usuario.getId() );
        usuarioResponse.nombreCompleto( usuario.getNombreCompleto() );
        usuarioResponse.updatedAt( usuario.getUpdatedAt() );
        usuarioResponse.usuario( usuario.getUsuario() );

        return usuarioResponse.build();
    }

    @Override
    public void updateEntity(UsuarioRequest request, Usuario entity) {
        if ( request == null ) {
            return;
        }

        entity.setActivo( request.getActivo() );
        entity.setContrasena( request.getContrasena() );
        entity.setNombreCompleto( request.getNombreCompleto() );
        entity.setUsuario( request.getUsuario() );
    }

    private Integer usuarioRolId(Usuario usuario) {
        Rol rol = usuario.getRol();
        if ( rol == null ) {
            return null;
        }
        return rol.getId();
    }

    private String usuarioRolNombre(Usuario usuario) {
        Rol rol = usuario.getRol();
        if ( rol == null ) {
            return null;
        }
        return rol.getNombre();
    }
}
