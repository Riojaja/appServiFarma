package com.example.proyecto.app.repository;

import com.example.proyecto.app.entity.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
    Optional<Configuracion> findByClave(String clave);
}