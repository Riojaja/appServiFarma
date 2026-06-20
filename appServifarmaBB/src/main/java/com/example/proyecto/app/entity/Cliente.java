package com.example.proyecto.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "documento_tipo", length = 20, nullable = false)
    private DocumentoTipo documentoTipo;

    @Column(name = "documento_numero", length = 20, nullable = false, unique = true)
    private String documentoNumero;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String direccion;

    @Column(length = 100)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Relación inversa con Venta (un cliente puede tener muchas ventas)
    @OneToMany(mappedBy = "cliente")
    private List<Venta> ventas;

    // Enum anidado para el tipo de documento
    public enum DocumentoTipo {
        DNI,
        RUC,
        Pasaporte
    }
}
