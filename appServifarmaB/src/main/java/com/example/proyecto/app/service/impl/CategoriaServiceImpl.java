package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.CategoriaRequest;
import com.example.proyecto.app.dto.response.CategoriaResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Categoria;
import com.example.proyecto.app.exception.BusinessException;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.CategoriaMapper;
import com.example.proyecto.app.repository.CategoriaRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.CategoriaService;
import com.example.proyecto.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaServiceImpl.class);

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final CategoriaMapper categoriaMapper;
    private final BitacoraComunicacionService bitacoraService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        // Validar que el nombre no exista (ignorando mayúsculas/minúsculas)
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = categoriaMapper.toEntity(request);
        Categoria saved = categoriaRepository.save(categoria);
        log.info("Categoría creada: {} (ID: {})", saved.getNombre(), saved.getId());

        // ==============================================================
        // MENSAJE AUTOMÁTICO EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "📁 Nueva categoría: %s (ID: %d)",
                    saved.getNombre(),
                    saved.getId()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para nueva categoría ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para nueva categoría: {}", e.getMessage());
        }

        return categoriaMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request) {
        // Verificar que la categoría existe
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + id + " no encontrada."));

        // Validar que el nombre no exista en otra categoría (si cambia)
        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre()) &&
                categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        // Guardar nombre anterior para el mensaje de bitácora
        String nombreAnterior = categoria.getNombre();

        // Actualizar datos
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria updated = categoriaRepository.save(categoria);
        log.info("Categoría actualizada: {} (ID: {})", updated.getNombre(), updated.getId());

        // ==============================================================
        // MENSAJE AUTOMÁTICO EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "✏️ Categoría actualizada: '%s' → '%s' (ID: %d)",
                    nombreAnterior,
                    updated.getNombre(),
                    updated.getId()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para actualización de categoría ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para actualización de categoría: {}", e.getMessage());
        }

        return categoriaMapper.toResponse(updated);
    }

    @Override
    public CategoriaResponse obtenerCategoriaPorId(Integer id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + id + " no encontrada."));
        return categoriaMapper.toResponse(categoria);
    }

    @Override
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAllByOrderByNombreAsc().stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoriaResponse> buscarPorNombre(String nombre) {
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(categoriaMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarCategoria(Integer id) {
        // Verificar que la categoría existe
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría con ID " + id + " no encontrada."));

        // Verificar si tiene productos asociados
        long count = productoRepository.countByCategoriaId(id);
        if (count > 0) {
            throw new BusinessException(
                    "No se puede eliminar la categoría porque tiene " + count + " productos asociados."
            );
        }

        String nombreCategoria = categoria.getNombre();
        categoriaRepository.deleteById(id);
        log.info("Categoría eliminada: {} (ID: {})", nombreCategoria, id);

        // ==============================================================
        // MENSAJE AUTOMÁTICO EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "🗑️ Categoría eliminada: %s (ID: %d)",
                    nombreCategoria,
                    id
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.incidencia)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para eliminación de categoría ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para eliminación de categoría: {}", e.getMessage());
        }
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return categoriaRepository.existsByNombreIgnoreCase(nombre);
    }

    // ============================================================
    // MÉTODO AUXILIAR PARA OBTENER EL ID DEL USUARIO AUTENTICADO
    // ============================================================

    private Integer getUsuarioId() {
        try {
            return securityUtils.getUsuarioAutenticado().getId();
        } catch (Exception e) {
            log.debug("No se pudo obtener usuario autenticado, usando usuario sistema (ID 1)");
            return 1; // Usuario sistema por defecto
        }
    }
}