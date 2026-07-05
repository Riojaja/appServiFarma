package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.Venta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaResponse {

    private Integer id;
    private LocalDateTime fecha;
    private BigDecimal total;
    private Venta.MedioPago medioPago;
    private String codigoAutorizacion;
    private Venta.EstadoVenta estado;
    private LocalDateTime createdAt;

    // Datos del usuario (vendedor)
    private Integer usuarioId;
    private String usuarioNombre;

    // Datos del cliente (opcional)
    private Integer clienteId;
    private String clienteNombre;

    // Datos de la caja
    private Integer cajaId;

    // Lista de detalles de la venta
    private List<DetalleVentaResponse> detalles;
}
