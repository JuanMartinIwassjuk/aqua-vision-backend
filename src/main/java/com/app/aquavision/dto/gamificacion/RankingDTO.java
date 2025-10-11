package com.app.aquavision.dto.gamificacion;

import com.app.aquavision.entities.domain.Hogar;

import java.util.ArrayList;
import java.util.List;

public class RankingDTO {
    public List<HogarRankingDTO> hogares = new ArrayList<>();

    public RankingDTO () {
    }

    public RankingDTO (List<Hogar> hogares) {
        for (int i = 1; i <= hogares.size(); i++) {
            Hogar hogar = hogares.get(i-1);
            this.hogares.add(new HogarRankingDTO(hogar.getNombre(),hogar.getPuntaje_ranking(), i));
        }
    }

}
