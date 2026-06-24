package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.ProveedorRequest;
import com.example.proyecto.app.dto.response.ProveedorResponse;

import java.util.List;

public interface ProveedorService {

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    /**
     * Registra un nuevo proveedor en el directorio comercial.
     * @param request Datos del proveedor (RUC, razón social, región, contacto, etc.).
     * @return ProveedorResponse con los datos guardados.
     * @throws DuplicadoException Si ya existe un proveedor con el mismo RUC.
     */
    ProveedorResponse crearProveedor(ProveedorRequest request);

    /**
     * Actualiza los datos de un proveedor existente.
     * @param id ID del proveedor a actualizar.
     * @param request Nuevos datos del proveedor.
     * @return ProveedorResponse con los datos actualizados.
     * @throws ResourceNotFoundException Si el proveedor no existe.
     * @throws DuplicadoException Si se intenta cambiar el RUC y ya existe otro con ese número.
     */
    ProveedorResponse actualizarProveedor(Integer id, ProveedorRequest request);

    /**
     * Obtiene un proveedor por su ID.
     * @param id ID del proveedor.
     * @return ProveedorResponse con los datos.
     * @throws ResourceNotFoundException Si el proveedor no existe.
     */
    ProveedorResponse obtenerProveedorPorId(Integer id);

    /**
     * Obtiene un proveedor por su RUC (único).
     * Útil para validar duplicados y para la carga de facturas electrónicas (RF17).
     * @param ruc RUC del proveedor.
     * @return ProveedorResponse con los datos.
     * @throws ResourceNotFoundException Si no se encuentra un proveedor con ese RUC.
     */
    ProveedorResponse obtenerProveedorPorRuc(String ruc);

    /**
     * Lista todos los proveedores, ordenados alfabéticamente por razón social (A-Z).
     * @return Lista de ProveedorResponse.
     */
    List<ProveedorResponse> listarTodos();

    // ==============================
    // BÚSQUEDAS POR CAMPOS ESPECÍFICOS (RF12)
    // ==============================

    /**
     * Busca proveedores por razón social (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Ideal para autocompletado en formularios de compras.
     * @param razonSocial Texto a buscar en la razón social.
     * @return Lista de ProveedorResponse que coinciden.
     */
    List<ProveedorResponse> buscarPorRazonSocial(String razonSocial);

    /**
     * Busca proveedores por nombre de contacto (coincidencia parcial).
     * Útil cuando se recuerda el nombre del vendedor/distribuidor.
     * @param contacto Nombre del contacto a buscar.
     * @return Lista de ProveedorResponse que coinciden.
     */
    List<ProveedorResponse> buscarPorContacto(String contacto);

    /**
     * Filtra proveedores por región (ej. Cusco, Lima, Ayacucho).
     * Esencial para el directorio comercial y estadísticas por zona (RF12).
     * @param region Nombre de la región.
     * @return Lista de ProveedorResponse que pertenecen a esa región.
     */
    List<ProveedorResponse> buscarPorRegion(String region);

    /**
     * Obtiene la lista de regiones únicas registradas en el directorio de proveedores.
     * Útil para poblar combos de filtrado en la interfaz de usuario (RF12).
     * @return Lista de nombres de regiones (String).
     */
    List<String> obtenerRegionesDistintas();

    // ==============================
    // OPERACIONES DE ELIMINACIÓN Y VALIDACIÓN
    // ==============================

    /**
     * Elimina un proveedor del sistema (borrado físico).
     * @param id ID del proveedor a eliminar.
     * @throws ResourceNotFoundException Si el proveedor no existe.
     * @throws BusinessException Si el proveedor tiene lotes asociados (para preservar integridad).
     */
    void eliminarProveedor(Integer id);

    /**
     * Verifica si existe un proveedor con el RUC dado.
     * @param ruc RUC a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existePorRuc(String ruc);
}