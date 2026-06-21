package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.response.VentaResponse;
import com.example.proyecto.app.entity.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class, uses = {DetalleVentaMapper.class})
public interface VentaMapper {

    /**
     * Convierte una entidad Venta a VentaResponse.
     * Mapea los IDs y nombres de las entidades relacionadas (Usuario, Cliente, Caja).
     * La lista de detalles se mapea automáticamente usando DetalleVentaMapper.
     */
    @Mapping(source = "usuario.id", target = "usuarioId")
    @Mapping(source = "usuario.nombreCompleto", target = "usuarioNombre")
    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nombre", target = "clienteNombre")
    @Mapping(source = "caja.id", target = "cajaId")
    VentaResponse toResponse(Venta entity);
}