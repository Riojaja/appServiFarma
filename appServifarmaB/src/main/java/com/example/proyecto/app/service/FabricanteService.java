package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.FabricanteRequest;
import com.example.proyecto.app.dto.response.FabricanteResponse;

import java.util.List;

public interface FabricanteService {

    /**
     * Registra un nuevo fabricante/laboratorio en el sistema.
     * @param request Datos del fabricante (nombre, contacto, teléfono, email).
     * @return FabricanteResponse con los datos guardados.
     * @throws DuplicadoException Si ya existe un fabricante con el mismo nombre (ignorando mayúsculas).
     */
    FabricanteResponse crearFabricante(FabricanteRequest request);

    /**
     * Actualiza los datos de un fabricante existente.
     * @param id ID del fabricante a actualizar.
     * @param request Nuevos datos del fabricante.
     * @return FabricanteResponse con los datos actualizados.
     * @throws ResourceNotFoundException Si el fabricante no existe.
     * @throws DuplicadoException Si se intenta cambiar el nombre y ya existe otro con ese nombre.
     */
    FabricanteResponse actualizarFabricante(Integer id, FabricanteRequest request);

    /**
     * Obtiene un fabricante por su ID.
     * @param id ID del fabricante.
     * @return FabricanteResponse con los datos.
     * @throws ResourceNotFoundException Si el fabricante no existe.
     */
    FabricanteResponse obtenerFabricantePorId(Integer id);

    /**
     * Lista todos los fabricantes, ordenados alfabéticamente por nombre (A-Z).
     * @return Lista de FabricanteResponse.
     */
    List<FabricanteResponse> listarTodos();

    /**
     * Busca fabricantes cuyo nombre contenga la cadena especificada (ignorando mayúsculas/minúsculas).
     * Útil para autocompletado en la interfaz de productos.
     * @param nombre Cadena a buscar.
     * @return Lista de FabricanteResponse que coinciden.
     */
    List<FabricanteResponse> buscarPorNombre(String nombre);

    /**
     * Elimina un fabricante del sistema (borrado físico).
     * @param id ID del fabricante a eliminar.
     * @throws ResourceNotFoundException Si el fabricante no existe.
     * @throws BusinessException Si el fabricante tiene productos asociados (opcional, para mantener integridad).
     */
    void eliminarFabricante(Integer id);

    /**
     * Verifica si existe un fabricante con el nombre dado (ignorando mayúsculas/minúsculas).
     * @param nombre Nombre a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existePorNombre(String nombre);
}