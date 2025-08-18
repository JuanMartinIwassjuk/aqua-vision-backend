package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Hogar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface HogarRepository extends CrudRepository<Hogar, Long>{

    @Query("SELECT h FROM Hogar h LEFT JOIN FETCH h.sectores s LEFT JOIN FETCH s.mediciones WHERE h.id = :hogarId")
    Hogar findByIdWithSectoresAndMediciones(@Param("hogarId") Long hogarId);

    @Query("""
        select distinct h
        from Hogar h
        left join fetch h.sectores s
        where h.id = :id
    """)
    Optional<Hogar> findByIdWithSectores(@Param("id") Long id);
}
