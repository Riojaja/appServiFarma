package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.AperturaCajaRequest;
import com.example.proyecto.app.dto.request.CierreCajaRequest;
import com.example.proyecto.app.dto.response.CajaResponse;
import com.example.proyecto.app.dto.response.CierreCajaResponse;
import com.example.proyecto.app.entity.Caja;
import com.example.proyecto.app.entity.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CajaService {

    // ==============================
    // OPERACIONES DE APERTURA Y CIERRE
    // ==============================

    /**
     * Abre una nueva caja para la jornada.
     * 
     * @param request Datos de apertura (monto inicial, usuario que abre).
     * @return CajaResponse con los datos de la caja abierta.
     * @throws CajaCerradaException Si ya existe una caja abierta para el usuario (o en general).
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    CajaResponse abrirCaja(AperturaCajaRequest request);

    /**
     * Cierra la caja activa, realizando la conciliación de fondos.
     * 
     * @param request Datos de cierre (monto declarado, usuario que cierra).
     * @return CierreCajaResponse con el resumen de la jornada (total ventas, diferencia, etc.).
     * @throws CajaCerradaException Si no hay una caja abierta para cerrar.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    CierreCajaResponse cerrarCaja(CierreCajaRequest request);

    // ==============================
    // CONSULTAS DE CAJA
    // ==============================

    /**
     * Obtiene la caja abierta actual (la más reciente con estado 'abierta').
     * 
     * @return CajaResponse con los datos de la caja abierta.
     * @throws CajaCerradaException Si no hay ninguna caja abierta.
     */
    CajaResponse obtenerCajaAbierta();

    /**
     * Obtiene una caja por su ID.
     * 
     * @param id ID de la caja.
     * @return CajaResponse con los datos.
     * @throws ResourceNotFoundException Si la caja no existe.
     */
    CajaResponse obtenerCajaPorId(Integer id);

    /**
     * Lista todas las cajas registradas, ordenadas por fecha de apertura descendente.
     * 
     * @return Lista de CajaResponse.
     */
    List<CajaResponse> listarTodas();

    /**
     * Lista las cajas abiertas por un usuario específico.
     * 
     * @param usuarioId ID del usuario que abrió la caja.
     * @return Lista de CajaResponse.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    List<CajaResponse> listarPorUsuarioApertura(Integer usuarioId);

    /**
     * Lista las cajas cerradas en un rango de fechas (para reportes).
     * 
     * @param inicio Fecha y hora de inicio (no nula).
     * @param fin Fecha y hora de fin (no nula).
     * @return Lista de CajaResponse.
     */
    List<CajaResponse> listarPorFechasCierre(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Lista las cajas por estado (abierta o cerrada).
     * 
     * @param estado Estado de la caja ('abierta' o 'cerrada').
     * @return Lista de CajaResponse.
     */
    List<CajaResponse> listarPorEstado(Caja.EstadoCaja estado);

    // ==============================
    // VALIDACIONES Y CONSULTAS DE ESTADO
    // ==============================

    /**
     * Verifica si existe una caja abierta actualmente.
     * 
     * @return true si hay una caja abierta, false en caso contrario.
     */
    boolean existeCajaAbierta();

    /**
     * Verifica si un usuario tiene una caja abierta actualmente.
     * 
     * @param usuarioId ID del usuario.
     * @return true si el usuario tiene una caja abierta, false en caso contrario.
     */
    boolean usuarioTieneCajaAbierta(Integer usuarioId);

    /**
     * Obtiene el total de ventas registradas en una caja específica (solo ventas completadas).
     * 
     * @param cajaId ID de la caja.
     * @return Total de ventas (BigDecimal).
     * @throws ResourceNotFoundException Si la caja no existe.
     */
    BigDecimal obtenerTotalVentasCaja(Integer cajaId);

    /**
     * Obtiene el total de ventas por medio de pago para una caja específica.
     * 
     * @param cajaId ID de la caja.
     * @param medioPago Medio de pago (efectivo, tarjeta, transferencia, yape).
     * @return Total de ventas para ese medio de pago.
     * @throws ResourceNotFoundException Si la caja no existe.
     */
    BigDecimal obtenerTotalVentasPorMedioPago(Integer cajaId, Venta.MedioPago medioPago);
}