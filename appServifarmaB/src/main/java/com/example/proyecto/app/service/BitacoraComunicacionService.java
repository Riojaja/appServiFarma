package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.BitacoraComunicacionRequest;
import com.example.proyecto.app.dto.response.BitacoraComunicacionResponse;
import com.example.proyecto.app.entity.BitacoraComunicacion;

import java.time.LocalDateTime;
import java.util.List;

public interface BitacoraComunicacionService {

    // ==============================
    // OPERACIONES DE CREACIÓN Y ACTUALIZACIÓN
    // ==============================

    /**
     * Crea un nuevo mensaje en la bitácora de comunicación.
     * Este mensaje quedará disponible para el turno siguiente.
     *
     * @param request Datos del mensaje (mensaje, tipo, usuario que lo crea).
     * @return BitacoraComunicacionResponse con los datos guardados.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    BitacoraComunicacionResponse crearMensaje(BitacoraComunicacionRequest request);

    /**
     * Marca un mensaje como leído (para que el siguiente turno sepa que ya fue revisado).
     *
     * @param id ID del mensaje.
     * @throws ResourceNotFoundException Si el mensaje no existe.
     */
    void marcarComoLeido(Integer id);

    // ==============================
    // CONSULTAS
    // ==============================

    /**
     * Obtiene un mensaje por su ID.
     *
     * @param id ID del mensaje.
     * @return BitacoraComunicacionResponse con los datos.
     * @throws ResourceNotFoundException Si el mensaje no existe.
     */
    BitacoraComunicacionResponse obtenerPorId(Integer id);

    /**
     * Lista todos los mensajes de la bitácora, ordenados por fecha descendente (más reciente primero).
     *
     * @return Lista de BitacoraComunicacionResponse.
     */
    List<BitacoraComunicacionResponse> listarTodos();

    /**
     * Lista todos los mensajes no leídos.
     * Útil para que el turno entrante consulte los pendientes con un solo clic (RF16).
     *
     * @return Lista de BitacoraComunicacionResponse no leídos.
     */
    List<BitacoraComunicacionResponse> listarNoLeidos();

    /**
     * Lista los mensajes de un usuario específico (el que los creó).
     *
     * @param usuarioId ID del usuario.
     * @return Lista de BitacoraComunicacionResponse.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    List<BitacoraComunicacionResponse> listarPorUsuario(Integer usuarioId);

    /**
     * Lista mensajes filtrados por tipo (novedad, recordatorio, incidencia).
     *
     * @param tipo Tipo de mensaje.
     * @return Lista de BitacoraComunicacionResponse.
     */
    List<BitacoraComunicacionResponse> listarPorTipo(BitacoraComunicacion.Tipo tipo);

    /**
     * Lista mensajes en un rango de fechas (para consultar los del turno de mañana, etc.).
     *
     * @param inicio Fecha y hora de inicio (no nula).
     * @param fin Fecha y hora de fin (no nula).
     * @return Lista de BitacoraComunicacionResponse.
     */
    List<BitacoraComunicacionResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Lista mensajes no leídos de un usuario específico.
     * Útil para que un usuario vea sus pendientes.
     *
     * @param usuarioId ID del usuario.
     * @return Lista de BitacoraComunicacionResponse no leídos del usuario.
     */
    List<BitacoraComunicacionResponse> listarNoLeidosPorUsuario(Integer usuarioId);

    // ==============================
    // OPERACIONES DE ELIMINACIÓN
    // ==============================

    /**
     * Elimina un mensaje de la bitácora (borrado físico).
     *
     * @param id ID del mensaje a eliminar.
     * @throws ResourceNotFoundException Si el mensaje no existe.
     */
    void eliminarMensaje(Integer id);
}
