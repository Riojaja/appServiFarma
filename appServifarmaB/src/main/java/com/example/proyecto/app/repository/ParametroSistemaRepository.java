package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.ParametroSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ParametroSistemaRepository extends JpaRepository<ParametroSistema, Integer> {

    /**
     * Busca un parámetro por su clave (única).
     * Retorna Optional para manejar el caso de que no exista.
     */
    Optional<ParametroSistema> findByClave(String clave);

    /**
     * Obtiene el valor de un parámetro directamente como String.
     * Útil para consultas rápidas sin cargar toda la entidad.
     */
    @Query("SELECT p.valor FROM ParametroSistema p WHERE p.clave = :clave")
    Optional<String> findValorByClave(@Param("clave") String clave);

    /**
     * Verifica si existe un parámetro con la clave dada.
     */
    boolean existsByClave(String clave);

    /**
     * Actualiza el valor de un parámetro existente.
     * Retorna el número de filas afectadas (0 si no existía).
     */
    @Modifying
    @Transactional
    @Query("UPDATE ParametroSistema p SET p.valor = :valor WHERE p.clave = :clave")
    int updateValorByClave(@Param("clave") String clave, @Param("valor") String valor);
}
