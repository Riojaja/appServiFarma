package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fabricante_id")
    private Fabricante fabricante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(length = 200, nullable = false)
    private String nombre;

    @Column(name = "codigo_barras", length = 50, unique = true)
    private String codigoBarras;

    @Column(name = "principio_activo", length = 150)
    private String principioActivo;

    @Column(length = 255)
    private String imagen;

    @Column(name = "es_generico")
    @Builder.Default
    private Boolean esGenerico = false;

    // Auto-relación: producto genérico asociado
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_generico_id")
    private Producto productoGenerico;

    @Column(name = "precio_venta_actual", precision = 10, scale = 2, nullable = false)
    private BigDecimal precioVentaActual;

    @Column(name = "stock_minimo", nullable = false)
    @Builder.Default
    private Integer stockMinimo = 5;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relaciones inversas
    @OneToMany(mappedBy = "productoGenerico")
    private List<Producto> productosGenericos; // Productos que tienen este como genérico

    @OneToMany(mappedBy = "producto")
    private List<Lote> lotes;
}