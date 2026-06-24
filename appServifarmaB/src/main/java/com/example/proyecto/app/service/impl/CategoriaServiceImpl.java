package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.CategoriaRequest;
import com.example.proyecto.app.dto.response.CategoriaResponse;
import com.example.proyecto.app.entity.Categoria;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.CategoriaMapper;
import com.example.proyecto.app.repository.CategoriaRepository;
import com.example.proyecto.app.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Override
    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest request) {
        // Validar que el nombre no exista (ignorando mayúsculas/minúsculas)
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = categoriaMapper.toEntity(request);
        Categoria saved = categoriaRepository.save(categoria);
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

        // Actualizar datos
        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        Categoria updated = categoriaRepository.save(categoria);
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
        if (!categoriaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Categoría con ID " + id + " no encontrada.");
        }
        
        // Opcional: Verificar si tiene productos asociados antes de eliminar.
        // Si el repositorio tiene un método countByCategoriaId, podemos usarlo.
        // long count = productoRepository.countByCategoriaId(id);
        // if (count > 0) {
        //     throw new BusinessException("No se puede eliminar la categoría porque tiene productos asociados.");
        // }
        
        categoriaRepository.deleteById(id);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return categoriaRepository.existsByNombreIgnoreCase(nombre);
    }
}
