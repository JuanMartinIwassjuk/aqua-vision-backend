package com.app.aquavision.dto.gamificacion;

import com.app.aquavision.entities.domain.Hogar;

import java.util.ArrayList;
import java.util.List;

public class RankingDTO {
    public List<HogarRankingDTO> hogares = new ArrayList<>();

    public RankingDTO () {
    }

    public RankingDTO(List<Object[]> hogaresConMediciones) {
        int i = 0;

        for (Object[] row : hogaresConMediciones) {

            Long hogarId = (Long) row[0];
            String nombreHogar = (String) row[1];
            Integer miembros = (Integer) row[2];
            String totalPuntos = row[3].toString();
            String juegoMasJugado = (String) row[4];

            if (totalPuntos == null) {
                totalPuntos = String.valueOf(0L);
            }

            i++;

            this.hogares.add(
                    new HogarRankingDTO(
                            nombreHogar,
                            totalPuntos,
                            i,
                            juegoMasJugado
                    )
            );
        }
    }

    public RankingDTO(List<Object[]> rankingGeneral, boolean isFullRanking) {
        int i = 0;

        for (Object[] row : rankingGeneral) {
            Long hogarId = (Long) row[0];
            String nombreHogar = (String) row[1];
            String localidad = (String) row[2];
            Integer miembros = (Integer) row[3];
            Integer cantidadSectores = ((Number) row[4]).intValue();
            String totalPuntos = row[5] != null ? row[5].toString() : "0";
            String juegoMasJugado = (String) row[6];


            i++;

            this.hogares.add(
                    new HogarRankingDTO(
                            nombreHogar,
                            totalPuntos,
                            i,
                            juegoMasJugado,
                            localidad,
                            cantidadSectores
                    )
            );
        }
    }
}
