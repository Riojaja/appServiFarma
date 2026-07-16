package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FabricanteRepository extends JpaRepository<Fabricante, Integer> {

    /**
     * Busca un fabricante por su nombre exacto.
     * (No es único en BD, pero normalmente se usa para validar duplicados en la capa de servicio).
     */
    Optional<Fabricante> findByNombre(String nombre);

    /**
     * Busca fabricantes cuyo nombre contenga la cadena especificada (ignorando mayúsculas/minúsculas).
     * Útil para autocompletado y búsquedas rápidas.
     */
    List<Fabricante> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Obtiene todos los fabricantes ordenados alfabéticamente por nombre (A-Z).
     */
    List<Fabricante> findAllByOrderByNombreAsc();

    /**
     * Verifica si ya existe un fabricante con el nombre dado (ignorando mayúsculas/minúsculas).
     */
    boolean existsByNombreIgnoreCase(String nombre);
    
    Optional<Fabricante> findByNombreIgnoreCase(String nombre);
}