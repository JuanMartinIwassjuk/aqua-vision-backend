package com.app.aquavision.dto.consumos;

import com.app.aquavision.entities.domain.Sector;

public class ConsumoTotalSectorDTO {

    //Sector
    private Long sector_id;
    private String nombreSector;

    //Consumos
    private int consumoTotal = 0;
    private int consumoPromedio = 0;
    private int consumoPico = 0;

    public ConsumoTotalSectorDTO(Sector sector) {
        //Se delega al controller traer al sector con las mediciones ya filtradas por fecha

        this.consumoTotal = sector.totalConsumo();
        this.consumoPromedio = sector.promedioConsumo();
        this.consumoPico = sector.picoConsumo();
        this.sector_id = sector.getId();
        this.nombreSector = sector.getNombre();
    }

    public int getConsumoTotal() {
        return consumoTotal;
    }

    public Long getSector_id() {
        return sector_id;
    }

    public String getNombreSector() {
        return nombreSector;
    }

    public int getConsumoPromedio() {
        return consumoPromedio;
    }

    public int getConsumoPico() {
        return consumoPico;
    }

}
