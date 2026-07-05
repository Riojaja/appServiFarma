package com.example.proyecto.app.service.impl;

import com.example.proyecto.app.dto.request.ClienteRequest;
import com.example.proyecto.app.dto.response.ClienteResponse;
import com.example.proyecto.app.entity.Cliente;
import com.example.proyecto.app.exception.DuplicadoException;
import com.example.proyecto.app.exception.ResourceNotFoundException;
import com.example.proyecto.app.mapper.ClienteMapper;
import com.example.proyecto.app.repository.ClienteRepository;
import com.example.proyecto.app.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Override
    @Transactional
    public ClienteResponse crearCliente(ClienteRequest request) {
        // Validar que el documento no exista
        if (clienteRepository.existsByDocumentoNumero(request.getDocumentoNumero())) {
            throw new DuplicadoException("Ya existe un cliente con el documento: " + request.getDocumentoNumero());
        }

        Cliente cliente = clienteMapper.toEntity(request);
        Cliente saved = clienteRepository.save(cliente);
        return clienteMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarCliente(Integer id, ClienteRequest request) {
        // Verificar que el cliente existe
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID " + id + " no encontrado."));

        // Validar que el documento no exista en otro cliente (si cambia)
        if (!cliente.getDocumentoNumero().equals(request.getDocumentoNumero()) &&
                clienteRepository.existsByDocumentoNumero(request.getDocumentoNumero())) {
            throw new DuplicadoException("Ya existe otro cliente con el documento: " + request.getDocumentoNumero());
        }

        // Actualizar datos
        cliente.setNombre(request.getNombre());
        cliente.setDocumentoTipo(request.getDocumentoTipo());
        cliente.setDocumentoNumero(request.getDocumentoNumero());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setEmail(request.getEmail());

        Cliente updated = clienteRepository.save(cliente);
        return clienteMapper.toResponse(updated);
    }

    @Override
    public ClienteResponse obtenerClientePorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID " + id + " no encontrado."));
        return clienteMapper.toResponse(cliente);
    }

    @Override
    public ClienteResponse obtenerClientePorDocumento(String documentoNumero) {
        Cliente cliente = clienteRepository.findByDocumentoNumero(documentoNumero)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con documento " + documentoNumero + " no encontrado."));
        return clienteMapper.toResponse(cliente);
    }

    @Override
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAllByOrderByNombreAsc().stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponse> buscarPorNombre(String nombre) {
        return clienteRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClienteResponse> buscarPorDocumentoTipo(Cliente.DocumentoTipo documentoTipo) {
        return clienteRepository.findByDocumentoTipo(documentoTipo).stream()
                .map(clienteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarCliente(Integer id) {
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente con ID " + id + " no encontrado.");
        }
        // Nota: Aquí podrías agregar una validación para no eliminar clientes
        // que tengan ventas asociadas, si así lo deseas.
        // Ejemplo:
        // if (ventaRepository.existsByClienteId(id)) {
        //     throw new BusinessException("No se puede eliminar el cliente porque tiene ventas asociadas.");
        // }
        clienteRepository.deleteById(id);
    }

    @Override
    public boolean existePorDocumento(String documentoNumero) {
        return clienteRepository.existsByDocumentoNumero(documentoNumero);
    }
}