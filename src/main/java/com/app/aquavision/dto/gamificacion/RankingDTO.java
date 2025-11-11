package com.app.aquavision.dto.gamificacion;

import com.app.aquavision.entities.domain.Hogar;

import java.util.ArrayList;
import java.util.List;

public class RankingDTO {
    public List<HogarRankingDTO> hogares = new ArrayList<>();

    public RankingDTO () {
    }

    public RankingDTO (List<Object[]> hogaresConMediciones) {
        int i = 0;
        for (Object[] row : hogaresConMediciones) {
            Hogar hogar = (Hogar) row[0];
            Long totalPuntos = (Long) row[1];
            String nombreHogar  = hogar.getNombre();
            if(totalPuntos == null){
                totalPuntos = 0L;
            }
            i++;
            this.hogares.add(new HogarRankingDTO(nombreHogar,totalPuntos.intValue(), i));
        }
    }

}
