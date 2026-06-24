package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ProveedorRequest;
import com.example.proyecto.app.dto.response.ProveedorResponse;
import com.example.proyecto.app.entity.Proveedor;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ProveedorMapper;
import com.example.proyecto.app.repository.ProveedorRepository;
import com.example.proyecto.app.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    @Override
    @Transactional
    public ProveedorResponse crearProveedor(ProveedorRequest request) {
        // Validar que el RUC no exista
        if (proveedorRepository.existsByRuc(request.getRuc())) {
            throw new DuplicadoException("Ya existe un proveedor con el RUC: " + request.getRuc());
        }

        Proveedor proveedor = proveedorMapper.toEntity(request);
        Proveedor saved = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProveedorResponse actualizarProveedor(Integer id, ProveedorRequest request) {
        // Verificar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con ID " + id + " no encontrado."));

        // Validar que el RUC no exista en otro proveedor (si cambia)
        if (!proveedor.getRuc().equals(request.getRuc()) &&
                proveedorRepository.existsByRuc(request.getRuc())) {
            throw new DuplicadoException("Ya existe otro proveedor con el RUC: " + request.getRuc());
        }

        // Actualizar datos
        proveedor.setRuc(request.getRuc());
        proveedor.setRazonSocial(request.getRazonSocial());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setContacto(request.getContacto());
        proveedor.setRegion(request.getRegion());

        Proveedor updated = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponse(updated);
    }

    @Override
    public ProveedorResponse obtenerProveedorPorId(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con ID " + id + " no encontrado."));
        return proveedorMapper.toResponse(proveedor);
    }

    @Override
    public ProveedorResponse obtenerProveedorPorRuc(String ruc) {
        Proveedor proveedor = proveedorRepository.findByRuc(ruc)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con RUC " + ruc + " no encontrado."));
        return proveedorMapper.toResponse(proveedor);
    }

    @Override
    public List<ProveedorResponse> listarTodos() {
        return proveedorRepository.findAllByOrderByRazonSocialAsc().stream()
                .map(proveedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ==============================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS (RF12)
    // ==============================

    @Override
    public List<ProveedorResponse> buscarPorRazonSocial(String razonSocial) {
        return proveedorRepository.findByRazonSocialContainingIgnoreCase(razonSocial).stream()
                .map(proveedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProveedorResponse> buscarPorContacto(String contacto) {
        return proveedorRepository.findByContactoContainingIgnoreCase(contacto).stream()
                .map(proveedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProveedorResponse> buscarPorRegion(String region) {
        return proveedorRepository.findByRegion(region).stream()
                .map(proveedorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> obtenerRegionesDistintas() {
        return proveedorRepository.findDistinctRegiones();
    }

    // ==============================
    // OPERACIONES DE ELIMINACIÓN Y VALIDACIÓN
    // ==============================

    @Override
    @Transactional
    public void eliminarProveedor(Integer id) {
        if (!proveedorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proveedor con ID " + id + " no encontrado.");
        }

        // Opcional: Verificar si tiene lotes asociados antes de eliminar.
        // Si en el futuro añades LoteRepository, puedes descomentar esto:
        // long count = loteRepository.countByProveedorId(id);
        // if (count > 0) {
        //     throw new BusinessException("No se puede eliminar el proveedor porque tiene " + count + " lotes asociados.");
        // }

        proveedorRepository.deleteById(id);
    }

    @Override
    public boolean existePorRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }
}