package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens_invalidados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenInvalidado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500, nullable = false, unique = true)
    private String token;

    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @CreationTimestamp
    @Column(name = "fecha_invalidacion", updatable = false)
    private LocalDateTime fechaInvalidacion;
}