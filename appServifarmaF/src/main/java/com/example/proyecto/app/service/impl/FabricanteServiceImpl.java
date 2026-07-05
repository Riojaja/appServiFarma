package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.FabricanteRequest;
import com.example.proyecto.app.dto.response.FabricanteResponse;
import com.example.proyecto.app.entity.Fabricante;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.FabricanteMapper;
import com.example.proyecto.app.repository.FabricanteRepository;
import com.example.proyecto.app.service.FabricanteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FabricanteServiceImpl implements FabricanteService {

    private final FabricanteRepository fabricanteRepository;
    private final FabricanteMapper fabricanteMapper;

    @Override
    @Transactional
    public FabricanteResponse crearFabricante(FabricanteRequest request) {
        // Validar que el nombre no exista (ignorando mayúsculas/minúsculas)
        if (fabricanteRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe un fabricante con el nombre: " + request.getNombre());
        }

        Fabricante fabricante = fabricanteMapper.toEntity(request);
        Fabricante saved = fabricanteRepository.save(fabricante);
        return fabricanteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FabricanteResponse actualizarFabricante(Integer id, FabricanteRequest request) {
        // Verificar que el fabricante existe
        Fabricante fabricante = fabricanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabricante con ID " + id + " no encontrado."));

        // Validar que el nombre no exista en otro fabricante (si cambia)
        if (!fabricante.getNombre().equalsIgnoreCase(request.getNombre()) &&
                fabricanteRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe otro fabricante con el nombre: " + request.getNombre());
        }

        // Actualizar datos
        fabricante.setNombre(request.getNombre());
        fabricante.setContacto(request.getContacto());
        fabricante.setTelefono(request.getTelefono());
        fabricante.setEmail(request.getEmail());

        Fabricante updated = fabricanteRepository.save(fabricante);
        return fabricanteMapper.toResponse(updated);
    }

    @Override
    public FabricanteResponse obtenerFabricantePorId(Integer id) {
        Fabricante fabricante = fabricanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabricante con ID " + id + " no encontrado."));
        return fabricanteMapper.toResponse(fabricante);
    }

    @Override
    public List<FabricanteResponse> listarTodos() {
        return fabricanteRepository.findAllByOrderByNombreAsc().stream()
                .map(fabricanteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<FabricanteResponse> buscarPorNombre(String nombre) {
        return fabricanteRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(fabricanteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarFabricante(Integer id) {
        if (!fabricanteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fabricante con ID " + id + " no encontrado.");
        }

        // Opcional: Verificar si tiene productos asociados antes de eliminar.
        // Si en el futuro añades ProductoRepository, puedes descomentar esto:
        // long count = productoRepository.countByFabricanteId(id);
        // if (count > 0) {
        //     throw new BusinessException("No se puede eliminar el fabricante porque tiene productos asociados.");
        // }

        fabricanteRepository.deleteById(id);
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return fabricanteRepository.existsByNombreIgnoreCase(nombre);
    }
}