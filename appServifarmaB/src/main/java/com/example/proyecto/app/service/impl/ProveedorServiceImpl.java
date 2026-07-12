package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ProveedorRequest;
import com.example.proyecto.app.dto.response.ProveedorResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Proveedor;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ProveedorMapper;
import com.example.proyecto.app.repository.ProveedorRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.ProveedorService;
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
public class ProveedorServiceImpl implements ProveedorService {

    private static final Logger log = LoggerFactory.getLogger(ProveedorServiceImpl.class);

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;
    private final BitacoraComunicacionService bitacoraService;
    private final SecurityUtils securityUtils;

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
        log.info("Proveedor creado: {} (RUC: {})", saved.getRazonSocial(), saved.getRuc());

        // ==============================================================
        // MENSAJE AUTOMÁTICO EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "🚚 Nuevo proveedor: %s (RUC: %s) - Contacto: %s",
                    saved.getRazonSocial(),
                    saved.getRuc(),
                    saved.getContacto() != null ? saved.getContacto() : "Sin contacto"
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para nuevo proveedor ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para nuevo proveedor: {}", e.getMessage());
        }

        return proveedorMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProveedorResponse actualizarProveedor(Integer id, ProveedorRequest request) {
        // Verificar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con ID " + id + " no encontrado."));

        // Guardar datos anteriores para el mensaje
        String razonSocialAnterior = proveedor.getRazonSocial();
        String rucAnterior = proveedor.getRuc();

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
        log.info("Proveedor actualizado: {} (ID: {})", updated.getRazonSocial(), updated.getId());

        // ==============================================================
        // MENSAJE AUTOMÁTICO EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "✏️ Proveedor actualizado: '%s' → '%s' (RUC: %s → %s)",
                    razonSocialAnterior,
                    updated.getRazonSocial(),
                    rucAnterior,
                    updated.getRuc()
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para actualización de proveedor ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para actualización de proveedor: {}", e.getMessage());
        }

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
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor con ID " + id + " no encontrado."));

        // Verificar si tiene lotes asociados (si tienes LoteRepository)
        // long count = loteRepository.countByProveedorId(id);
        // if (count > 0) {
        //     throw new BusinessException("No se puede eliminar el proveedor porque tiene " + count + " lotes asociados.");
        // }

        String razonSocial = proveedor.getRazonSocial();
        String ruc = proveedor.getRuc();
        proveedorRepository.deleteById(id);
        log.info("Proveedor eliminado: {} (RUC: {})", razonSocial, ruc);

        // ==============================================================
        // MENSAJE AUTOMÁTICO EN BITÁCORA
        // ==============================================================
        try {
            String mensaje = String.format(
                    "🗑️ Proveedor eliminado: %s (RUC: %s)",
                    razonSocial,
                    ruc
            );

            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.incidencia)
                            .build();

            bitacoraService.crearMensaje(bitacoraRequest);
            log.info("Mensaje de bitácora creado para eliminación de proveedor ID: {}", id);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para eliminación de proveedor: {}", e.getMessage());
        }
    }

    @Override
    public boolean existePorRuc(String ruc) {
        return proveedorRepository.existsByRuc(ruc);
    }

    // ============================================================
    // MÉTODO AUXILIAR PARA OBTENER EL ID DEL USUARIO AUTENTICADO
    // ============================================================

    private Integer getUsuarioId() {
        try {
            return securityUtils.getUsuarioAutenticado().getId();
        } catch (Exception e) {
            log.debug("No se pudo obtener usuario autenticado, usando usuario sistema (ID 1)");
            return 1;
        }
    }
}