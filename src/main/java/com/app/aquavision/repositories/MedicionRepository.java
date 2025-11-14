package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;

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

        @Query("SELECT COALESCE(SUM(m.flow), 0) FROM Medicion m WHERE m.sector = :sector AND m.timestamp >= :start AND m.timestamp <= :end")
    Long sumFlowBySectorAndTimestampBetween(
        @Param("sector") Sector sector,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

        @Query("SELECT DISTINCT m FROM Medicion m " +
           "LEFT JOIN FETCH m.sector s " +
           "LEFT JOIN FETCH s.hogar h " +
           "WHERE m.timestamp >= :desde AND m.timestamp <= :hasta " +
           "ORDER BY m.timestamp ASC")
    List<Medicion> findAllWithSectorAndHogarBetween(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);


            @Query("""
    SELECT DATE(m.timestamp) AS fecha, SUM(m.flow) AS total
    FROM Medicion m
    WHERE m.timestamp BETWEEN :desde AND :hasta
    GROUP BY DATE(m.timestamp)
    ORDER BY fecha
""")
List<Object[]> sumFlowGroupByDay(
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
);

@Query("""
    SELECT h.id, h.nombre, h.localidad, SUM(m.flow)
    FROM Medicion m
    JOIN m.sector s
    JOIN s.hogar h
    WHERE m.timestamp BETWEEN :desde AND :hasta
    GROUP BY h.id, h.nombre, h.localidad
""")
List<Object[]> consumoPorHogar(
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
);


@Query("""
    SELECT MONTH(m.timestamp) AS mes, SUM(m.flow) AS total
    FROM Medicion m
    WHERE m.timestamp BETWEEN :desde AND :hasta
    GROUP BY MONTH(m.timestamp)
    ORDER BY total DESC
""")
List<Object[]> topMeses(
        @Param("desde") LocalDateTime desde,
        @Param("hasta") LocalDateTime hasta
);



}
