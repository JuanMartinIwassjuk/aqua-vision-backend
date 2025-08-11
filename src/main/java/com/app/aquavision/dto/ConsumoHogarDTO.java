package com.app.aquavision.dto;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumoHogarDTO {

    //Hogar
    private final int hogarId;
    private final String miembros;
    private final String localidad;

    //Fechas
    private final LocalDateTime fechaDesde;
    private final LocalDateTime fechaHasta;
    private final LocalDateTime fechaGeneracion = LocalDateTime.now();

    //Consumos
    private int consumoTotal = 0;
    private int consumoPromedio = 0;
    private int consumoPico = 0;

    private final List<ConsumoSectorDTO> consumosPorSector = new ArrayList<>();

    public ConsumoHogarDTO(Hogar hogar, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.consumoTotal = 0;

        //TODO: Mover logica a service
        for (Sector sector: hogar.getSectores()) {
            ConsumoSectorDTO consumoDiarioSectorDTO = new ConsumoSectorDTO(sector);
            this.consumosPorSector.add(consumoDiarioSectorDTO);

            this.consumoTotal += consumoDiarioSectorDTO.getConsumoTotal();
        }

        if (!this.consumosPorSector.isEmpty()) {
            this.consumoPromedio = this.consumosPorSector.stream()
                    .mapToInt(ConsumoSectorDTO::getConsumoPromedio)
                    .sum();
            this.consumoPico = this.consumosPorSector.stream()
                    .mapToInt(ConsumoSectorDTO::getConsumoPico)
                    .max()
                    .orElse(0);
        }

        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;

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

    public LocalDateTime getFechaDesde() {
        return fechaDesde;
    }

    public LocalDateTime getFechaHasta() {
        return fechaHasta;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public int getConsumoPromedio() {
        return consumoPromedio;
    }

    public int getConsumoPico() {
        return consumoPico;
    }

}
