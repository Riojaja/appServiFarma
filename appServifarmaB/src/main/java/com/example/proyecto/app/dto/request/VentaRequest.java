package com.example.proyecto.app.dto.request;

import com.example.proyecto.app.entity.Venta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequest {

    @NotNull(message = "El ID del usuario (vendedor) es obligatorio")
    private Integer usuarioId;

    // Opcional: puede ser null para ventas sin cliente registrado
    private Integer clienteId;

    @NotNull(message = "El medio de pago es obligatorio")
    private Venta.MedioPago medioPago;

    @Size(max = 50, message = "El código de autorización no puede exceder los 50 caracteres")
    private String codigoAutorizacion; // Opcional, para YAPE, transferencias, etc.

    @NotNull(message = "La venta debe tener al menos un producto")
    @Size(min = 1, message = "La venta debe tener al menos un producto")
    @Valid // Para que se validen los DetalleVentaRequest internos
    private List<DetalleVentaRequest> detalles;
}
