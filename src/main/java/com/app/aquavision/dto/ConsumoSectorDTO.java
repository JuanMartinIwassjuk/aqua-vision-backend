package com.app.aquavision.dto;

import com.app.aquavision.entities.domain.Sector;

public class ConsumoSectorDTO {

    private int consumoTotal = 0;
    private final Sector sector;

    public ConsumoSectorDTO(Sector sector) {
        this.consumoTotal = sector.totalConsumo();
        //Se delega al controller traer al sector con las mediciones ya filtradas por fecha
        this.sector = sector;
    }

    public int getConsumoTotal() {
        return consumoTotal;
    }

    public Sector getSector() {
        return sector;
    }


}
