package com.app.aquavision.repositories;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.TipoHogar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HogarRepository extends CrudRepository<Hogar, Long> {

    @Query("SELECT h FROM Hogar h LEFT JOIN FETCH h.sectores s LEFT JOIN FETCH s.mediciones WHERE h.id = :hogarId")
    Hogar findByIdWithSectoresAndMediciones(@Param("hogarId") Long hogarId);

    @Query("""
                select distinct h
                from Hogar h
                left join fetch h.sectores s
                where h.id = :id
            """)
    Optional<Hogar> findByIdWithSectores(@Param("id") Long id);

    @Query(value = """
            SELECT 
              h.id AS hogar_id,
              h.nombre,
              h.miembros,
              COALESCE(total.totalPuntos, 0) AS totalPuntos,
              jm.juego_mas_jugado
            FROM aquavision.hogar h
            LEFT JOIN (
              SELECT hogar_id, SUM(puntos) AS totalPuntos
              FROM aquavision.puntos_reclamados
              GROUP BY hogar_id
            ) total ON total.hogar_id = h.id
            LEFT JOIN (
              SELECT pg.hogar_id,
                     MIN(pg.mini_juego) AS juego_mas_jugado
              FROM (
                SELECT hogar_id, mini_juego, SUM(puntos) AS puntos_juego
                FROM aquavision.puntos_reclamados
                GROUP BY hogar_id, mini_juego
              ) pg
              JOIN (
                SELECT hogar_id, MAX(puntos_juego) AS max_puntos
                FROM (
                  SELECT hogar_id, mini_juego, SUM(puntos) AS puntos_juego
                  FROM aquavision.puntos_reclamados
                  GROUP BY hogar_id, mini_juego
                ) t
                GROUP BY hogar_id
              ) mg ON pg.hogar_id = mg.hogar_id AND pg.puntos_juego = mg.max_puntos
              GROUP BY pg.hogar_id
            ) jm ON jm.hogar_id = h.id
            WHERE h.miembros = (
              SELECT h2.miembros
              FROM aquavision.hogar h2
              WHERE h2.id = :hogarId
            )
            ORDER BY totalPuntos DESC
            """, nativeQuery = true)
    List<Object[]> findAllBySameMembersAndOrderByPuntajeDesc(@Param("hogarId") Long hogarId);

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

    List<Hogar> findAll();


    @Query(value = """
            SELECT 
              h.id AS hogar_id,
              h.nombre,
              h.localidad,
              h.miembros,
              COALESCE(sec.cantidad_sectores, 0) AS cantidad_sectores,
              COALESCE(total.totalPuntos, 0) AS totalPuntos,
              jm.juego_mas_jugado
            FROM aquavision.hogar h
            
            LEFT JOIN (
              SELECT hogar_id, SUM(puntos) AS totalPuntos
              FROM aquavision.puntos_reclamados
              GROUP BY hogar_id
            ) total ON total.hogar_id = h.id
            
            LEFT JOIN (
              SELECT pg.hogar_id,
                     MIN(pg.mini_juego) AS juego_mas_jugado
              FROM (
                SELECT hogar_id, mini_juego, SUM(puntos) AS puntos_juego
                FROM aquavision.puntos_reclamados
                GROUP BY hogar_id, mini_juego
              ) pg
              JOIN (
                SELECT hogar_id, MAX(puntos_juego) AS max_puntos
                FROM (
                  SELECT hogar_id, mini_juego, SUM(puntos) AS puntos_juego
                  FROM aquavision.puntos_reclamados
                  GROUP BY hogar_id, mini_juego
                ) t
                GROUP BY hogar_id
              ) mg ON pg.hogar_id = mg.hogar_id AND pg.puntos_juego = mg.max_puntos
              GROUP BY pg.hogar_id
            ) jm ON jm.hogar_id = h.id
            
            
            LEFT JOIN (
              SELECT hogar_id, COUNT(*) AS cantidad_sectores
              FROM aquavision.sector
              GROUP BY hogar_id
            ) sec ON sec.hogar_id = h.id
            
            ORDER BY totalPuntos DESC
            """, nativeQuery = true)
    List<Object[]> findRankingHogares();

}
