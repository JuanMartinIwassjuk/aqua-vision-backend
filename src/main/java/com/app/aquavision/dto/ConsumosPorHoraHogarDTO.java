package com.app.aquavision.dto;

import com.app.aquavision.entities.domain.Hogar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumosPorHoraHogarDTO {

    private final Long hogarId;
    private final int miembros;
    private final String localidad;
    private final LocalDateTime fechaDesde;
    private final LocalDateTime fechaHasta;
    private final LocalDateTime fechaGeneracion = LocalDateTime.now();
    private final List<ConsumoPorHoraDTO> consumosPorHora = new ArrayList<>();

        public ConsumosPorHoraHogarDTO(Hogar hogar, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
            this.hogarId = hogar.getId();
            this.miembros = hogar.getMiembros();
            this.localidad = hogar.getLocalidad();

            this.fechaDesde = fechaDesde;
            this.fechaHasta = fechaHasta;

            //TODO: Mover logica a service
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

    public LocalDateTime getFechaDesde() {
        return fechaDesde;
    }

    public LocalDateTime getFechaHasta() {
        return fechaHasta;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

}
