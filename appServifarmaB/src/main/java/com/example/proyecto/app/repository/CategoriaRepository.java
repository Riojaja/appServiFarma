package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    /**
     * Busca una categoría por su nombre exacto (case-sensitive).
     * Útil para validar duplicados al crear o actualizar.
     */
    Optional<Categoria> findByNombre(String nombre);

    /**
     * Busca categorías cuyo nombre contenga la cadena especificada (ignorando mayúsculas/minúsculas).
     * Ideal para autocompletado y búsquedas en la interfaz de usuario.
     */
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Obtiene todas las categorías ordenadas alfabéticamente por nombre (A-Z).
     */
    List<Categoria> findAllByOrderByNombreAsc();

    /**
     * Verifica si ya existe una categoría con el nombre dado (case-insensitive).
     */
    boolean existsByNombreIgnoreCase(String nombre);
}