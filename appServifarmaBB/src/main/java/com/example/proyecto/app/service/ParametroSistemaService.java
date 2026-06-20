package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.ParametroSistemaRequest;
import com.example.proyecto.app.dto.response.ParametroSistemaResponse;

import java.util.List;

public interface ParametroSistemaService {

    /**
     * Crea un nuevo parámetro de configuración en el sistema.
     * La clave debe ser única.
     */
    ParametroSistemaResponse crearParametro(ParametroSistemaRequest request);

    /**
     * Actualiza un parámetro existente (su valor, descripción, etc.).
     */
    ParametroSistemaResponse actualizarParametro(Integer id, ParametroSistemaRequest request);

    /**
     * Obtiene un parámetro por su ID.
     */
    ParametroSistemaResponse obtenerPorId(Integer id);

    /**
     * Obtiene un parámetro por su clave (única). 
     * Útil cuando se conoce el nombre de la configuración (ej. "dias_alerta_vencimiento").
     */
    ParametroSistemaResponse obtenerPorClave(String clave);

    /**
     * Obtiene el valor de un parámetro directamente como String, sin cargar toda la entidad.
     * Método de conveniencia para consultas rápidas desde otros servicios (ej. LoteService, VentaService).
     */
    String obtenerValorPorClave(String clave);

    /**
     * Lista todos los parámetros de configuración del sistema.
     */
    List<ParametroSistemaResponse> listarTodos();

    /**
     * Verifica si existe un parámetro con la clave dada.
     */
    boolean existePorClave(String clave);

    /**
     * Actualiza el valor de un parámetro directamente por su clave.
     * Método eficiente para cambios rápidos sin tener que cargar toda la entidad.
     */
    void actualizarValorPorClave(String clave, String nuevoValor);

    /**
     * Elimina un parámetro del sistema (borrado físico).
     */
    void eliminarParametro(Integer id);
}
