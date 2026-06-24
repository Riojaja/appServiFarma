package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.DemandaInsatisfechaRequest;
import com.example.proyecto.app.dto.response.DemandaInsatisfechaResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface DemandaInsatisfechaService {

    // ==============================
    // OPERACIONES DE REGISTRO
    // ==============================

    /**
     * Registra una nueva demanda insatisfecha (producto solicitado pero sin stock).
     *
     * @param request Datos de la demanda (producto solicitado, documento del cliente, usuario que registra).
     * @return DemandaInsatisfechaResponse con los datos guardados.
     * @throws ResourceNotFoundException Si el usuario que registra no existe.
     */
    DemandaInsatisfechaResponse crearDemanda(DemandaInsatisfechaRequest request);

    // ==============================
    // CONSULTAS
    // ==============================

    /**
     * Obtiene una demanda insatisfecha por su ID.
     *
     * @param id ID de la demanda.
     * @return DemandaInsatisfechaResponse con los datos.
     * @throws ResourceNotFoundException Si la demanda no existe.
     */
    DemandaInsatisfechaResponse obtenerPorId(Integer id);

    /**
     * Lista todas las demandas insatisfechas registradas, ordenadas por fecha descendente.
     *
     * @return Lista de DemandaInsatisfechaResponse.
     */
    List<DemandaInsatisfechaResponse> listarTodas();

    /**
     * Lista las demandas insatisfechas de un usuario específico (el que las registró).
     *
     * @param usuarioId ID del usuario.
     * @return Lista de DemandaInsatisfechaResponse.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    List<DemandaInsatisfechaResponse> listarPorUsuario(Integer usuarioId);

    /**
     * Lista las demandas insatisfechas por nombre de producto (coincidencia parcial, ignorando mayúsculas).
     * Útil para buscar productos demandados específicos.
     *
     * @param productoSolicitado Nombre del producto (o parte de él).
     * @return Lista de DemandaInsatisfechaResponse.
     */
    List<DemandaInsatisfechaResponse> listarPorProducto(String productoSolicitado);

    /**
     * Lista las demandas insatisfechas en un rango de fechas (para reportes y estadísticas).
     *
     * @param inicio Fecha y hora de inicio (no nula).
     * @param fin Fecha y hora de fin (no nula).
     * @return Lista de DemandaInsatisfechaResponse.
     */
    List<DemandaInsatisfechaResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Lista las demandas insatisfechas asociadas a un documento de cliente específico.
     *
     * @param clienteDocumento Número de documento del cliente (DNI, RUC, etc.).
     * @return Lista de DemandaInsatisfechaResponse.
     */
    List<DemandaInsatisfechaResponse> listarPorClienteDocumento(String clienteDocumento);

    // ==============================
    // ESTADÍSTICAS Y ELIMINACIÓN
    // ==============================

    /**
     * Cuenta el número de demandas insatisfechas en un rango de fechas.
     * Útil para estadísticas de "ventas perdidas" (requisito RF9).
     *
     * @param inicio Fecha y hora de inicio (no nula).
     * @param fin Fecha y hora de fin (no nula).
     * @return Número de demandas registradas en ese período.
     */
    long contarDemandasPorPeriodo(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Elimina una demanda insatisfecha (borrado físico).
     *
     * @param id ID de la demanda a eliminar.
     * @throws ResourceNotFoundException Si la demanda no existe.
     */
    void eliminarDemanda(Integer id);
}