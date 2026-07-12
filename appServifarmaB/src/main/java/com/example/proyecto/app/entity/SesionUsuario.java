package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SesionUsuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String token;

    private String dispositivo;
    private String ip;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaExpiracion;
    private boolean activa;
    private String refreshToken;
    private LocalDateTime ultimaActividad;
}