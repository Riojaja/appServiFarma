package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "caja")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_apertura", precision = 10, scale = 2, nullable = false)
    private BigDecimal montoApertura;

    @Column(name = "monto_cierre_declarado", precision = 10, scale = 2)
    private BigDecimal montoCierreDeclarado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_apertura_id", nullable = false)
    private Usuario usuarioApertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_cierre_id")
    private Usuario usuarioCierre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private EstadoCaja estado = EstadoCaja.abierta;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relación inversa: una caja puede tener muchas ventas
    @OneToMany(mappedBy = "caja")
    private List<Venta> ventas;

    // Enum anidado para el estado de la caja
    public enum EstadoCaja {
        abierta,
        cerrada
    }
}
