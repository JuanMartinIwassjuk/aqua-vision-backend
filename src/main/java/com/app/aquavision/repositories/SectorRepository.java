package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectorRepository extends JpaRepository<Sector, Long> {
    @Query("""
    SELECT h.id 
    FROM Sector s 
    JOIN Hogar h ON h.id = s.hogar.id 
    JOIN Medidor m ON m.id = s.medidor.id 
    WHERE m.numeroSerie = :numeroSerie
""")
    Optional<Long> findHogarIdByNumeroSerie(@Param("numeroSerie") int numeroSerie);

}
