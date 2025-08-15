package com.app.aquavision.dto.consumos;

import com.app.aquavision.entities.domain.Sector;

public class ConsumoTotalSectorDTO {

    //Sector
    private final Sector sector;

    //Consumos
    private int consumoTotal = 0;
    private int consumoPromedio = 0;
    private int consumoPico = 0;


    public ConsumoTotalSectorDTO(Sector sector) {
        //Se delega al controller traer al sector con las mediciones ya filtradas por fecha

        this.consumoTotal = sector.totalConsumo();
        this.consumoPromedio = sector.promedioConsumo();
        this.consumoPico = sector.picoConsumo();
        this.sector = sector;
    }

    public int getConsumoTotal() {
        return consumoTotal;
    }

    public Sector getSector() {
        return sector;
    }

    public int getConsumoPromedio() {
        return consumoPromedio;
    }

    public int getConsumoPico() {
        return consumoPico;
    }

}
