package com.example.proyecto.app.repository;

import com.example.proyecto.app.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    /**
     * Busca un rol por su nombre exacto (case-sensitive).
     * El nombre es único en la base de datos.
     */
    Optional<Rol> findByNombre(String nombre);

    /**
     * Busca roles cuyo nombre contenga la cadena especificada (ignorando mayúsculas/minúsculas).
     * Útil para autocompletado y búsquedas en la interfaz de administración.
     */
    List<Rol> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Obtiene todos los roles ordenados alfabéticamente por nombre (A-Z).
     * Útil para desplegar en combos o listados de selección.
     */
    List<Rol> findAllByOrderByNombreAsc();

    /**
     * Verifica si ya existe un rol con el nombre dado (ignorando mayúsculas/minúsculas).
     * Previene duplicados al crear o actualizar roles.
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Obtiene un rol por su nombre, ignorando mayúsculas/minúsculas.
     * Útil para validaciones en servicios donde el nombre es clave.
     */
    Optional<Rol> findByNombreIgnoreCase(String nombre);
}