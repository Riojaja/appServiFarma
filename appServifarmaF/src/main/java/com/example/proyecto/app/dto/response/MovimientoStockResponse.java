package com.example.proyecto.app.dto.response;

import com.example.proyecto.app.entity.MovimientoStock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoStockResponse {

    private Integer id;
    private MovimientoStock.TipoMovimiento tipoMovimiento;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private LocalDateTime fecha;
    private String observacion;
    private Integer referenciaId;
    private LocalDateTime createdAt;

    // Datos del lote (mapeados desde Lote en MovimientoStockMapper)
    private Integer loteId;
    private String nombreProducto;

    // Datos del usuario (mapeados desde Usuario en MovimientoStockMapper)
    private Integer usuarioId;
    private String nombreUsuario;
}