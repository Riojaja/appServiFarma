package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.ClienteRequest;
import com.example.proyecto.app.dto.response.ClienteResponse;
import com.example.proyecto.app.entity.Cliente;

import java.util.List;

public interface ClienteService {

    /**
     * Registra un nuevo cliente en el sistema.
     * @param request Datos del cliente (nombre, tipo y número de documento, etc.).
     * @return ClienteResponse con los datos guardados.
     * @throws DuplicadoException Si ya existe un cliente con el mismo número de documento.
     */
    ClienteResponse crearCliente(ClienteRequest request);

    /**
     * Actualiza los datos de un cliente existente.
     * @param id ID del cliente a actualizar.
     * @param request Nuevos datos del cliente.
     * @return ClienteResponse con los datos actualizados.
     * @throws ResourceNotFoundException Si el cliente no existe.
     * @throws DuplicadoException Si se intenta cambiar el documento y ya existe otro con ese número.
     */
    ClienteResponse actualizarCliente(Integer id, ClienteRequest request);

    /**
     * Obtiene un cliente por su ID.
     * @param id ID del cliente.
     * @return ClienteResponse con los datos.
     * @throws ResourceNotFoundException Si el cliente no existe.
     */
    ClienteResponse obtenerClientePorId(Integer id);

    /**
     * Obtiene un cliente por su número de documento (DNI, RUC o Pasaporte).
     * Esencial para el módulo de ventas, donde se busca al cliente por su DNI/RUC.
     * @param documentoNumero Número de documento del cliente.
     * @return ClienteResponse con los datos.
     * @throws ResourceNotFoundException Si no se encuentra un cliente con ese documento.
     */
    ClienteResponse obtenerClientePorDocumento(String documentoNumero);

    /**
     * Lista todos los clientes registrados, ordenados alfabéticamente por nombre (A-Z).
     * @return Lista de ClienteResponse.
     */
    List<ClienteResponse> listarTodos();

    /**
     * Busca clientes por nombre (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Útil para autocompletado en la interfaz de ventas.
     * @param nombre Cadena a buscar.
     * @return Lista de ClienteResponse que coinciden.
     */
    List<ClienteResponse> buscarPorNombre(String nombre);

    /**
     * Busca clientes por tipo de documento (DNI, RUC o Pasaporte).
     * @param documentoTipo Tipo de documento.
     * @return Lista de ClienteResponse que coinciden.
     */
    List<ClienteResponse> buscarPorDocumentoTipo(Cliente.DocumentoTipo documentoTipo);

    /**
     * Elimina un cliente del sistema (borrado físico).
     * @param id ID del cliente a eliminar.
     * @throws ResourceNotFoundException Si el cliente no existe.
     * @throws BusinessException Si el cliente tiene ventas asociadas (opcional, para mantener integridad).
     */
    void eliminarCliente(Integer id);

    /**
     * Verifica si existe un cliente con el número de documento dado.
     * @param documentoNumero Número de documento a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existePorDocumento(String documentoNumero);
}