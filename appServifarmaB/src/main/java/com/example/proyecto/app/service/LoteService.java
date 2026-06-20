package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.LoteRequest;
import com.example.proyecto.app.dto.response.LoteResponse;
import com.example.proyecto.app.entity.Lote;

import java.util.List;

public interface LoteService {

    // ==============================
    // OPERACIONES CRUD BÁSICAS
    // ==============================

    /**
     * Registra un nuevo lote en el sistema.
     * @param request Datos del lote (producto, proveedor, número de lote, fechas, cantidad, precios).
     * @return LoteResponse con los datos guardados.
     * @throws DuplicadoException Si ya existe un lote con el mismo número para el mismo producto (opcional).
     * @throws ResourceNotFoundException Si el producto o proveedor no existen.
     * @throws ParametroInvalidoException Si la fecha de vencimiento es anterior a la fecha de ingreso.
     */
    LoteResponse crearLote(LoteRequest request);

    /**
     * Actualiza los datos de un lote existente.
     * @param id ID del lote a actualizar.
     * @param request Nuevos datos del lote.
     * @return LoteResponse con los datos actualizados.
     * @throws ResourceNotFoundException Si el lote no existe.
     * @throws ParametroInvalidoException Si la fecha de vencimiento es anterior a la fecha de ingreso.
     */
    LoteResponse actualizarLote(Integer id, LoteRequest request);

    /**
     * Obtiene un lote por su ID.
     * @param id ID del lote.
     * @return LoteResponse con los datos.
     * @throws ResourceNotFoundException Si el lote no existe.
     */
    LoteResponse obtenerLotePorId(Integer id);

    /**
     * Lista todos los lotes registrados, ordenados por fecha de ingreso descendente (más reciente primero).
     * @return Lista de LoteResponse.
     */
    List<LoteResponse> listarTodos();

    /**
     * Elimina un lote del sistema (borrado físico).
     * @param id ID del lote a eliminar.
     * @throws ResourceNotFoundException Si el lote no existe.
     * @throws BusinessException Si el lote tiene movimientos de stock asociados (para preservar integridad).
     */
    void eliminarLote(Integer id);

    // ==============================
    // CONSULTAS ESPECÍFICAS
    // ==============================

    /**
     * Lista todos los lotes de un producto específico, ordenados por fecha de vencimiento (más próximo primero).
     * Útil para mostrar el detalle de stock de un producto.
     * @param productoId ID del producto.
     * @return Lista de LoteResponse.
     * @throws ResourceNotFoundException Si el producto no existe.
     */
    List<LoteResponse> listarPorProducto(Integer productoId);

    /**
     * Lista lotes filtrados por estado (activo, deteriorado, vencido, agotado).
     * @param estado Estado del lote (no nulo).
     * @return Lista de LoteResponse.
     */
    List<LoteResponse> listarPorEstado(Lote.EstadoLote estado);

    /**
     * Busca un lote por su número de lote (coincidencia exacta).
     * @param numeroLote Número de lote.
     * @return LoteResponse con los datos.
     * @throws ResourceNotFoundException Si no se encuentra el lote.
     */
    LoteResponse buscarPorLote(String numeroLote);

    /**
     * Lista lotes cuyo número de lote contenga la cadena especificada (búsqueda parcial).
     * @param numeroLote Parte del número de lote.
     * @return Lista de LoteResponse.
     */
    List<LoteResponse> buscarPorLoteContaining(String numeroLote);

    /**
     * Obtiene los lotes que están próximos a vencer, según los días de anticipación configurados.
     * @param diasAnticipacion Días de anticipación (se toman desde la fecha actual + diasAnticipacion).
     * @return Lista de LoteResponse de lotes activos que vencen en ese rango.
     */
    List<LoteResponse> obtenerLotesProximosAVencer(int diasAnticipacion);

    /**
     * Obtiene los lotes que ya han vencido (fecha de vencimiento anterior a hoy).
     * @return Lista de LoteResponse con estado 'vencido' o que deberían estarlo.
     */
    List<LoteResponse> obtenerLotesVencidos();

    // ==============================
    // OPERACIONES DE ACTUALIZACIÓN DE ESTADO
    // ==============================

    /**
     * Marca un lote como deteriorado (por daño físico).
     * @param id ID del lote.
     * @throws ResourceNotFoundException Si el lote no existe.
     */
    void marcarComoDeteriorado(Integer id);

    /**
     * Actualiza el estado de todos los lotes vencidos a 'vencido' (para tareas programadas).
     * @return Número de lotes actualizados.
     */
    int actualizarLotesVencidos();
}
