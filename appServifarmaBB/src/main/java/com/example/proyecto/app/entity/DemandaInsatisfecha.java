package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "demanda_insatisfecha")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandaInsatisfecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "producto_solicitado", length = 200, nullable = false)
    private String productoSolicitado;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime fecha;

    @Column(name = "cliente_documento", length = 20)
    private String clienteDocumento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}