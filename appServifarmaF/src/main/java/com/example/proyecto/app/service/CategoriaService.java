package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.CategoriaRequest;
import com.example.proyecto.app.dto.response.CategoriaResponse;

import java.util.List;

public interface CategoriaService {

    /**
     * Crea una nueva categoría.
     */
    CategoriaResponse crearCategoria(CategoriaRequest request);

    /**
     * Actualiza una categoría existente.
     */
    CategoriaResponse actualizarCategoria(Integer id, CategoriaRequest request);

    /**
     * Obtiene una categoría por su ID.
     */
    CategoriaResponse obtenerCategoriaPorId(Integer id);

    /**
     * Lista todas las categorías ordenadas alfabéticamente.
     */
    List<CategoriaResponse> listarTodas();

    /**
     * Busca categorías cuyo nombre contenga la cadena especificada.
     */
    List<CategoriaResponse> buscarPorNombre(String nombre);

    /**
     * Elimina una categoría por su ID.
     */
    void eliminarCategoria(Integer id);

    /**
     * Verifica si existe una categoría con el nombre dado (ignorando mayúsculas).
     */
    boolean existePorNombre(String nombre);
}