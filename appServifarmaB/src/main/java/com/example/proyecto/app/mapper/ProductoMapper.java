package com.example.proyecto.app.mapper;

import com.example.proyecto.app.dto.request.ProductoRequest;
import com.example.proyecto.app.dto.response.ProductoResponse;
import com.example.proyecto.app.entity.Producto;
import com.example.proyecto.app.config.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapStructConfig.class)
public interface ProductoMapper {

    /**
     * Convierte un ProductoRequest a una entidad Producto.
     * Ignora el ID (autogenerado), las fechas de auditoría y las relaciones inversas.
     * Las relaciones Categoria, Fabricante y ProductoGenerico se setean manualmente en el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categoria", ignore = true)          // Se setea en el servicio
    @Mapping(target = "fabricante", ignore = true)         // Se setea en el servicio
    @Mapping(target = "productoGenerico", ignore = true)   // Se setea en el servicio
    @Mapping(target = "productosGenericos", ignore = true) // Relación inversa
    @Mapping(target = "lotes", ignore = true)              // Relación inversa
    Producto toEntity(ProductoRequest request);

    /**
     * Convierte una entidad Producto a ProductoResponse.
     * Mapea los IDs de las relaciones para que el frontend pueda mostrarlos.
     */
    @Mapping(source = "categoria.id", target = "categoriaId")
    @Mapping(source = "categoria.nombre", target = "categoriaNombre")
    @Mapping(source = "fabricante.id", target = "fabricanteId")
    @Mapping(source = "fabricante.nombre", target = "fabricanteNombre")
    @Mapping(source = "productoGenerico.id", target = "productoGenericoId")
    @Mapping(source = "productoGenerico.nombre", target = "productoGenericoNombre")
    ProductoResponse toResponse(Producto entity);

    /**
     * Actualiza una entidad Producto existente con los datos del request.
     * Ignora el ID, las fechas de auditoría y las relaciones (que se manejan en el servicio).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "fabricante", ignore = true)
    @Mapping(target = "productoGenerico", ignore = true)
    @Mapping(target = "productosGenericos", ignore = true)
    @Mapping(target = "lotes", ignore = true)
    void updateEntity(@MappingTarget Producto entity, ProductoRequest request);
}