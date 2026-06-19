package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    // ==============================
    // BÚSQUEDAS POR CAMPOS ÚNICOS
    // ==============================

    /**
     * Busca un proveedor por su RUC (único en la base de datos).
     * Útil para validar duplicados al registrar un nuevo proveedor.
     */
    Optional<Proveedor> findByRuc(String ruc);

    /**
     * Verifica si ya existe un proveedor con el RUC dado.
     */
    boolean existsByRuc(String ruc);

    // ==============================
    // BÚSQUEDAS POR TEXTO PARCIAL
    // ==============================

    /**
     * Busca proveedores por razón social (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Ideal para autocompletado y búsquedas rápidas en el módulo de proveedores.
     */
    List<Proveedor> findByRazonSocialContainingIgnoreCase(String razonSocial);

    /**
     * Busca proveedores por nombre de contacto (coincidencia parcial, ignorando mayúsculas/minúsculas).
     * Útil cuando se recuerda el nombre del vendedor/distribuidor pero no la empresa.
     */
    List<Proveedor> findByContactoContainingIgnoreCase(String contacto);

    // ==============================
    // BÚSQUEDAS POR REGIÓN Y FILTROS GEOGRÁFICOS
    // ==============================

    /**
     * Obtiene todos los proveedores de una región específica.
     * Útil para el requisito RF12 (gestión de proveedores y directorio comercial).
     */
    List<Proveedor> findByRegion(String region);

    /**
     * Obtiene todas las regiones únicas disponibles en el directorio de proveedores.
     * Para poblar combos de filtrado en la interfaz de usuario.
     */
    @Query("SELECT DISTINCT p.region FROM Proveedor p WHERE p.region IS NOT NULL")
    List<String> findDistinctRegiones();

    // ==============================
    // LISTADOS ORDENADOS
    // ==============================

    /**
     * Obtiene todos los proveedores ordenados alfabéticamente por razón social (A-Z).
     */
    List<Proveedor> findAllByOrderByRazonSocialAsc();

    /**
     * Obtiene todos los proveedores ordenados por región y luego por razón social.
     * Útil para reportes agrupados por zona geográfica.
     */
    List<Proveedor> findAllByOrderByRegionAscRazonSocialAsc();
}