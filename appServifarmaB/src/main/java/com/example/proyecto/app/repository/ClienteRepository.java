package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Busca un cliente por su número de documento (DNI, RUC o Pasaporte).
     * El documento es único en la base de datos.
     */
    Optional<Cliente> findByDocumentoNumero(String documentoNumero);

    /**
     * Busca clientes por nombre completo (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Útil para autocompletado y búsquedas rápidas en ventas.
     */
    List<Cliente> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Busca clientes por tipo de documento (DNI, RUC o Pasaporte).
     */
    List<Cliente> findByDocumentoTipo(Cliente.DocumentoTipo documentoTipo);

    /**
     * Obtiene todos los clientes ordenados alfabéticamente por nombre (A-Z).
     */
    List<Cliente> findAllByOrderByNombreAsc();

    /**
     * Verifica si ya existe un cliente con el número de documento dado.
     */
    boolean existsByDocumentoNumero(String documentoNumero);
}