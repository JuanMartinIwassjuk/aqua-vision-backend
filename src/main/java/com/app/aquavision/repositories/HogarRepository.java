package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Hogar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface HogarRepository extends CrudRepository<Hogar, Long>{

    @Query("""
        select distinct h
        from Hogar h
        left join fetch h.sectores s
        where h.id = :id
    """)
    Hogar findByIdWithSectores(@Param("id") Long id);

}
