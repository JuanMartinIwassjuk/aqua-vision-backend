package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.TipoHogar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("""
    SELECT h AS hogar, SUM(pr.puntos) AS totalPuntos
        FROM Hogar h
        LEFT JOIN h.puntosReclamados pr
        GROUP BY h.id
    ORDER BY SUM(pr.puntos) DESC
    """)
    List<Object[]> findAllByOrderByPuntajeDesc();

    @Query("""
        SELECT h
        FROM Hogar h
        WHERE (:localidad IS NULL OR h.localidad = :localidad)
          AND (:miembros IS NULL OR h.miembros >= :miembros)
          AND (:tipoHogar IS NULL OR h.tipoHogar = :tipoHogar)
    """)
    List<Hogar> buscarHogaresPorFiltros(
            @Param("localidad") String localidad,
            @Param("miembros") Integer miembros,
            @Param("tipoHogar") TipoHogar tipoHogar
    );

}
