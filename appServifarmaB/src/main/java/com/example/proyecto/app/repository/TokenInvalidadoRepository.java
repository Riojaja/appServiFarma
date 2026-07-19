package com.example.proyecto.app.repository;

import com.example.proyecto.app.entity.TokenInvalidado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenInvalidadoRepository extends JpaRepository<TokenInvalidado, Long> {

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenInvalidado t WHERE t.usuarioId = :usuarioId")
    void deleteAllByUsuarioId(@Param("usuarioId") Integer usuarioId);
}