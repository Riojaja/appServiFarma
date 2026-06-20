package com.example.proyecto.app.mapper;

import com.example.proyecto.app.dto.request.ParametroSistemaRequest;
import com.example.proyecto.app.dto.response.ParametroSistemaResponse;
import com.example.proyecto.app.entity.ParametroSistema;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-20T01:16:16-0500",
    comments = "version: 1.6.0, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 25.0.1 (Eclipse Adoptium)"
)
@Component
public class ParametroSistemaMapperImpl implements ParametroSistemaMapper {

    @Override
    public ParametroSistema toEntity(ParametroSistemaRequest request) {
        if ( request == null ) {
            return null;
        }

        ParametroSistema.ParametroSistemaBuilder parametroSistema = ParametroSistema.builder();

        parametroSistema.clave( request.getClave() );
        parametroSistema.descripcion( request.getDescripcion() );
        parametroSistema.valor( request.getValor() );

        return parametroSistema.build();
    }

    @Override
    public ParametroSistemaResponse toResponse(ParametroSistema entity) {
        if ( entity == null ) {
            return null;
        }

        ParametroSistemaResponse.ParametroSistemaResponseBuilder parametroSistemaResponse = ParametroSistemaResponse.builder();

        parametroSistemaResponse.clave( entity.getClave() );
        parametroSistemaResponse.createdAt( entity.getCreatedAt() );
        parametroSistemaResponse.descripcion( entity.getDescripcion() );
        parametroSistemaResponse.id( entity.getId() );
        parametroSistemaResponse.updatedAt( entity.getUpdatedAt() );
        parametroSistemaResponse.valor( entity.getValor() );

        return parametroSistemaResponse.build();
    }
}
