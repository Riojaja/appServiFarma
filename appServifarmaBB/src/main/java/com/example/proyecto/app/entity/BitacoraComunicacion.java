package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bitacora_comunicacion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraComunicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_hora", nullable = false)
    @CreationTimestamp
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Tipo tipo = Tipo.novedad;

    @Builder.Default
    private Boolean leido = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Enum anidado para el tipo de mensaje
    public enum Tipo {
        novedad,
        recordatorio,
        incidencia
    }
}
