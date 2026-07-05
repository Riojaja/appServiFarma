package com.example.proyecto.app.service;

import com.example.proyecto.app.dto.request.VentaRequest;
import com.example.proyecto.app.dto.response.DetalleVentaResponse;
import com.example.proyecto.app.dto.response.VentaResponse;
import com.example.proyecto.app.entity.Venta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {

    // ==============================
    // OPERACIÓN PRINCIPAL: REGISTRAR VENTA
    // ==============================

    /**
     * Registra una nueva venta en el sistema.
     * Aplica la lógica FEFO, descuenta stock, registra movimientos y vincula la caja abierta.
     *
     * @param request Datos de la venta (cliente, productos, medio de pago, etc.)
     * @return VentaResponse con los datos de la venta registrada.
     * @throws CajaCerradaException Si no hay una caja abierta para registrar la venta.
     * @throws StockInsuficienteException Si no hay stock suficiente para alguno de los productos.
     * @throws LoteVencidoException Si algún lote seleccionado está vencido.
     * @throws ResourceNotFoundException Si el cliente o algún producto no existe.
     */
    VentaResponse registrarVenta(VentaRequest request);

    // ==============================
    // ANULACIÓN DE VENTAS
    // ==============================

    /**
     * Anula una venta existente (cambia su estado a 'anulada').
     * 
     * @param id ID de la venta a anular.
     * @throws VentaAnuladaException Si la venta ya está anulada o ya pasó el tiempo límite.
     * @throws ResourceNotFoundException Si la venta no existe.
     */
    void anularVenta(Integer id);

    // ==============================
    // CONSULTAS DE VENTAS
    // ==============================

    /**
     * Obtiene una venta por su ID, con todos sus detalles cargados (JOIN FETCH).
     * 
     * @param id ID de la venta.
     * @return VentaResponse con los datos de la venta y sus detalles.
     * @throws ResourceNotFoundException Si la venta no existe.
     */
    VentaResponse obtenerVentaPorId(Integer id);

    /**
     * Lista todas las ventas registradas, ordenadas por fecha descendente.
     * 
     * @return Lista de VentaResponse.
     */
    List<VentaResponse> listarTodas();

    /**
     * Lista las ventas de un cliente específico, ordenadas por fecha descendente.
     * 
     * @param clienteId ID del cliente.
     * @return Lista de VentaResponse.
     * @throws ResourceNotFoundException Si el cliente no existe.
     */
    List<VentaResponse> listarPorCliente(Integer clienteId);

    /**
     * Lista las ventas realizadas por un usuario (vendedor) específico.
     * 
     * @param usuarioId ID del usuario.
     * @return Lista de VentaResponse.
     * @throws ResourceNotFoundException Si el usuario no existe.
     */
    List<VentaResponse> listarPorUsuario(Integer usuarioId);

    /**
     * Lista las ventas en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio (no nula).
     * @param fin Fecha y hora de fin (no nula).
     * @return Lista de VentaResponse.
     */
    List<VentaResponse> listarPorFecha(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Lista las ventas filtradas por medio de pago.
     * 
     * @param medioPago Medio de pago (efectivo, tarjeta, transferencia, yape).
     * @return Lista de VentaResponse.
     */
    List<VentaResponse> listarPorMedioPago(Venta.MedioPago medioPago);

    /**
     * Lista las ventas filtradas por estado (completada o anulada).
     * 
     * @param estado Estado de la venta ('completada' o 'anulada').
     * @return Lista de VentaResponse.
     */
    List<VentaResponse> listarPorEstado(Venta.EstadoVenta estado);

    /**
     * Lista las ventas más recientes (útil para el dashboard).
     * 
     * @param limite Número máximo de ventas a devolver.
     * @return Lista de VentaResponse.
     */
    List<VentaResponse> listarUltimasVentas(int limite);

    // ==============================
    // CONSULTAS DE AGREGACIÓN (para reportes/estadísticas)
    // ==============================

    /**
     * Obtiene el total de ventas en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Total de ventas como BigDecimal.
     */
    java.math.BigDecimal obtenerTotalVentasPorPeriodo(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Obtiene el total de ventas agrupado por medio de pago en un rango de fechas.
     * 
     * @param inicio Fecha y hora de inicio.
     * @param fin Fecha y hora de fin.
     * @return Lista de Object[] donde cada elemento contiene [medioPago, total].
     */
    List<Object[]> obtenerTotalVentasPorMedioPagoYPeriodo(LocalDateTime inicio, LocalDateTime fin);
    
    List<DetalleVentaResponse> obtenerDetallesVenta(Integer ventaId);
}