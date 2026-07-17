package com.example.proyecto.app.mapper;

import com.example.proyecto.app.config.MapStructConfig;
import com.example.proyecto.app.dto.request.LoteRequest;
import com.example.proyecto.app.dto.response.LoteResponse;
import com.example.proyecto.app.entity.Lote;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface LoteMapper {

    /**
     * Convierte un LoteRequest a una entidad Lote.
     * Ignora el ID (autogenerado), las fechas de auditoría y las relaciones (se setean en el servicio).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "producto", ignore = true)          // Se setea en el servicio
    @Mapping(target = "proveedor", ignore = true)         // Se setea en el servicio
    @Mapping(target = "movimientosStock", ignore = true)  // Relación inversa
    @Mapping(target = "detalleVentas", ignore = true)     // Relación inversa
    Lote toEntity(LoteRequest request);

    /**
     * Convierte una entidad Lote a LoteResponse.
     * Mapea los IDs y nombres de las entidades relacionadas (Producto, Proveedor).
     * ✅ Ahora también mapea la imagen del producto para mostrarla en el frontend.
     */
    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "producto.nombre", target = "productoNombre")
    @Mapping(source = "producto.imagen", target = "productoImagen") // ✅ NUEVO: imagen del producto
    @Mapping(source = "proveedor.id", target = "proveedorId")
    @Mapping(source = "proveedor.razonSocial", target = "proveedorRazonSocial")
    LoteResponse toResponse(Lote entity);

    /**
     * Actualiza una entidad Lote existente con los datos del request.
     * Ignora el ID, las fechas de auditoría y las relaciones (se manejan en el servicio).
     * MapStruct actualiza solo los campos que no son null en el request.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "proveedor", ignore = true)
    @Mapping(target = "movimientosStock", ignore = true)
    @Mapping(target = "detalleVentas", ignore = true)
    void updateEntity(@MappingTarget Lote entity, LoteRequest request);
}