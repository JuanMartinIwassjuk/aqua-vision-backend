package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Medicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicionRepository extends JpaRepository<Medicion, Long> {

    @Query("""
        select m
        from Medicion m
        where m.sector.id = :sectorId
          and m.timestamp between :fechaDesde and :fechaHasta
    """)
    List<Medicion> findBySectorIdAndFechaBetween(
            @Param("sectorId") Long sectorId,
            @Param("fechaDesde") LocalDateTime fechaDesde,
            @Param("fechaHasta") LocalDateTime fechaHasta
    );
}
