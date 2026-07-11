package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.FabricanteRequest;
import com.example.proyecto.app.dto.response.FabricanteResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;
import com.example.proyecto.app.entity.Fabricante;
import com.example.proyecto.app.exception.BusinessException;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.FabricanteMapper;
import com.example.proyecto.app.repository.FabricanteRepository;
import com.example.proyecto.app.repository.ProductoRepository;
import com.example.proyecto.app.service.BitacoraComunicacionService;
import com.example.proyecto.app.service.FabricanteService;
import com.example.proyecto.app.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FabricanteServiceImpl implements FabricanteService {

    private final FabricanteRepository fabricanteRepository;
    private final ProductoRepository productoRepository; // Inyectar
    private final FabricanteMapper fabricanteMapper;
    private final BitacoraComunicacionService bitacoraService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public FabricanteResponse crearFabricante(FabricanteRequest request) {
        if (fabricanteRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe un fabricante con el nombre: " + request.getNombre());
        }

        Fabricante fabricante = fabricanteMapper.toEntity(request);
        Fabricante saved = fabricanteRepository.save(fabricante);
        log.info("Fabricante creado: {} (ID: {})", saved.getNombre(), saved.getId());

        // Mensaje en bitácora
        try {
            String mensaje = String.format("🏭 Nuevo fabricante: %s (ID: %d)", saved.getNombre(), saved.getId());
            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();
            bitacoraService.crearMensaje(bitacoraRequest);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para nuevo fabricante: {}", e.getMessage());
        }

        return fabricanteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public FabricanteResponse actualizarFabricante(Integer id, FabricanteRequest request) {
        Fabricante fabricante = fabricanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabricante con ID " + id + " no encontrado."));

        if (!fabricante.getNombre().equalsIgnoreCase(request.getNombre()) &&
                fabricanteRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new DuplicadoException("Ya existe otro fabricante con el nombre: " + request.getNombre());
        }

        String nombreAnterior = fabricante.getNombre();
        fabricante.setNombre(request.getNombre());
        fabricante.setContacto(request.getContacto());
        fabricante.setTelefono(request.getTelefono());
        fabricante.setEmail(request.getEmail());

        Fabricante updated = fabricanteRepository.save(fabricante);
        log.info("Fabricante actualizado: {} (ID: {})", updated.getNombre(), updated.getId());

        // Mensaje en bitácora
        try {
            String mensaje = String.format("✏️ Fabricante actualizado: '%s' → '%s' (ID: %d)",
                    nombreAnterior, updated.getNombre(), updated.getId());
            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.novedad)
                            .build();
            bitacoraService.crearMensaje(bitacoraRequest);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para actualización de fabricante: {}", e.getMessage());
        }

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
        Fabricante fabricante = fabricanteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fabricante con ID " + id + " no encontrado."));

        // Verificar si tiene productos asociados
        long count = productoRepository.countByFabricanteId(id);
        if (count > 0) {
            throw new BusinessException("No se puede eliminar el fabricante porque tiene " + count + " productos asociados.");
        }

        String nombreFabricante = fabricante.getNombre();
        fabricanteRepository.deleteById(id);
        log.info("Fabricante eliminado: {} (ID: {})", nombreFabricante, id);

        // Mensaje en bitácora
        try {
            String mensaje = String.format("🗑️ Fabricante eliminado: %s (ID: %d)", nombreFabricante, id);
            com.example.proyecto.app.dto.request.BitacoraComunicacionRequest bitacoraRequest =
                    com.example.proyecto.app.dto.request.BitacoraComunicacionRequest.builder()
                            .usuarioId(getUsuarioId())
                            .mensaje(mensaje)
                            .tipo(BitacoraComunicacion.Tipo.incidencia)
                            .build();
            bitacoraService.crearMensaje(bitacoraRequest);
        } catch (Exception e) {
            log.error("Error al crear mensaje en bitácora para eliminación de fabricante: {}", e.getMessage());
        }
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return fabricanteRepository.existsByNombreIgnoreCase(nombre);
    }

    private Integer getUsuarioId() {
        try {
            return securityUtils.getUsuarioAutenticado().getId();
        } catch (Exception e) {
            log.debug("No se pudo obtener usuario autenticado, usando usuario sistema (ID 1)");
            return 1;
        }
    }
}