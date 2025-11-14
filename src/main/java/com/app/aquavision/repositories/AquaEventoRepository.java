package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.gamification.AquaEvento;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AquaEventoRepository extends JpaRepository<AquaEvento, Long> {


    @Query("""
        SELECT e FROM AquaEvento e
        LEFT JOIN FETCH e.tags t
        WHERE e.fechaInicio >= :desde AND (e.fechaFin IS NULL OR e.fechaFin <= :hasta)

    """)
    List<AquaEvento> findEventosBetweenDates(@Param("desde") LocalDateTime desde,
                                             @Param("hasta") LocalDateTime hasta);


    @Query("""
        SELECT DISTINCT e FROM AquaEvento e
        LEFT JOIN FETCH e.tags t
        WHERE e.fechaInicio >= :desde
          AND (e.fechaFin IS NULL OR e.fechaFin <= :hasta)

          AND t.id IN :tagIds
    """)
    List<AquaEvento> findEventosBetweenDatesAndTags(@Param("desde") LocalDateTime desde,
                                                    @Param("hasta") LocalDateTime hasta,
                                                    @Param("tagIds") List<Integer> tagIds);
}