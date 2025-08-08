package com.app.aquavision.dto;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;

import java.util.ArrayList;
import java.util.List;

public class ConsumoHogarDTO {

    private final int hogarId;
    private final String miembros;
    private final String localidad;

    private int consumoTotal = 0;

    private final List<ConsumoSectorDTO> consumosPorSector = new ArrayList<>();

    public ConsumoHogarDTO(Hogar hogar) {
        this.consumoTotal = 0;

        for (Sector sector: hogar.getSectores()) {
            ConsumoSectorDTO consumoDiarioSectorDTO = new ConsumoSectorDTO(sector);
            this.consumosPorSector.add(consumoDiarioSectorDTO);

            this.consumoTotal += consumoDiarioSectorDTO.getConsumoTotal();
        }

        this.hogarId = hogar.getId().intValue();
        this.miembros = String.valueOf(hogar.getMiembros());
        this.localidad = hogar.getLocalidad();
    }

    public int getConsumoTotal() {
        return consumoTotal;
    }

    public int getHogarId() {
        return hogarId;
    }
    public String getMiembros() {
        return miembros;
    }
    public String getLocalidad() {
        return localidad;
    }
    public List<ConsumoSectorDTO> getConsumosPorSector() {
        return consumosPorSector;
    }

}
