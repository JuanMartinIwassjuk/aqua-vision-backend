package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.gamification.AquaEvento;
import com.app.aquavision.entities.domain.gamification.PuntosReclamados;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PuntosReclamadosRepository  extends JpaRepository<PuntosReclamados, Long> {


    @Query(value = """
    SELECT MAX(fecha)
    FROM puntos_reclamados pr
    WHERE pr.hogar_id = :id
      AND pr.mini_juego LIKE :minijuego
      AND pr.escena LIKE :escena
""", nativeQuery = true)
    LocalDateTime getUltimaFechaReclamo(
            @Param("id") Long id,
            @Param("minijuego") String minijuego,
            @Param("escena") String escena
    );

    
    @Query("SELECT p FROM PuntosReclamados p WHERE p.fecha BETWEEN :desde AND :hasta")
    List<PuntosReclamados> findByFechaBetween(LocalDateTime desde, LocalDateTime hasta);
}
