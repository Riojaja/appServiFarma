package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "lotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(length = 50, nullable = false)
    private String lote;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_compra", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioVenta;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoLote estado = EstadoLote.activo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones inversas
    @OneToMany(mappedBy = "lote")
    private List<MovimientoStock> movimientosStock;

    @OneToMany(mappedBy = "lote")
    private List<DetalleVenta> detalleVentas;

    // Enum anidado para el estado del lote
    public enum EstadoLote {
        activo,
        deteriorado,
        vencido,
        agotado
    }
}