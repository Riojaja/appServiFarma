package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "configuracion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Configuracion {
    @Id
    private String clave;

    @Column(nullable = false)
    private String valor;

    private String descripcion;
}