package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_unitario_venta", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioUnitarioVenta;

    @Column(name = "precio_compra_unitario", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioCompraUnitario;

    // Campo GENERATED ALWAYS en la base de datos (no se inserta ni actualiza desde JPA)
    @Column(name = "subtotal", precision = 10, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}