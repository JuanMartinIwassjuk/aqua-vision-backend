package com.app.aquavision.dto.consumos;

import com.app.aquavision.entities.domain.Hogar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumoTotalHogarDTO {

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
    private float consumoPromedio = 0;
    private float consumoPico = 0;

    private final List<ConsumoTotalSectorDTO> consumosPorSector = new ArrayList<>();

    public ConsumoTotalHogarDTO(Hogar hogar, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;

        this.hogarId = hogar.getId().intValue();
        this.miembros = String.valueOf(hogar.getMiembros());
        this.localidad = hogar.getLocalidad();
    }

    public void addConsumoSector(ConsumoTotalSectorDTO consumoSector) {
        this.consumosPorSector.add(consumoSector);
    }

    public void sumarConsumoTotal(int consumo) {
        this.consumoTotal += consumo;
    }

    public Integer getConsumoTotal() {
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
    public List<ConsumoTotalSectorDTO> getConsumosPorSector() {
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

    public Float getConsumoPromedio() {
        return consumoPromedio;
    }

    public Float getConsumoPico() {
        return consumoPico;
    }

    public void setConsumoPromedio(float consumoPromedio) {
        this.consumoPromedio = consumoPromedio;
    }

    public void setConsumoPico(float consumoPico) {
        this.consumoPico = consumoPico;
    }

}
