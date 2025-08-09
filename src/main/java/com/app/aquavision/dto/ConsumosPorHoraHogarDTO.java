package com.app.aquavision.dto;

import com.app.aquavision.entities.domain.Hogar;

import java.util.ArrayList;
import java.util.List;

public class ConsumosPorHoraHogarDTO {

    private final Long hogarId;
    private final int miembros;
    private final String localidad;
    private final List<ConsumoPorHoraDTO> consumosPorHora = new ArrayList<>();

        public ConsumosPorHoraHogarDTO(Hogar hogar) {
            this.hogarId = hogar.getId();
            this.miembros = hogar.getMiembros();
            this.localidad = hogar.getLocalidad();

            for (int i = 0; i < 24; i++) {
                int consumo = hogar.consumoTotalHora(i);
                consumosPorHora.add(new ConsumoPorHoraDTO(i, consumo));
            }
        }

    public List<ConsumoPorHoraDTO> getConsumosPorHora() {
        return consumosPorHora;
    }

    public Long getHogarId() {
        return hogarId;
    }

    public int getMiembros() {
        return miembros;
    }

    public String getLocalidad() {
        return localidad;
    }

}
