package com.example.proyecto.app.repository;


import com.example.proyecto.app.entity.DemandaInsatisfecha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DemandaInsatisfechaRepository extends JpaRepository<DemandaInsatisfecha, Integer> {

    /**
     * Obtiene todas las demandas insatisfechas registradas por un usuario específico.
     */
    List<DemandaInsatisfecha> findByUsuarioId(Integer usuarioId);

    /**
     * Obtiene las demandas insatisfechas en un rango de fechas (para reportes y estadísticas).
     */
    List<DemandaInsatisfecha> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca demandas insatisfechas por el nombre del producto solicitado (coincidencia parcial, ignorando mayúsculas/minúsculas).
     */
    List<DemandaInsatisfecha> findByProductoSolicitadoContainingIgnoreCase(String productoSolicitado);

    /**
     * Obtiene las demandas insatisfechas asociadas a un documento de cliente específico.
     */
    List<DemandaInsatisfecha> findByClienteDocumento(String clienteDocumento);

    /**
     * Cuenta el número de demandas insatisfechas en un rango de fechas (para estadísticas de ventas perdidas).
     */
    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    
}