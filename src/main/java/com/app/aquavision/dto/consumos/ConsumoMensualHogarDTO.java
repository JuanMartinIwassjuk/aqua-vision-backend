package com.app.aquavision.dto.consumos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumoMensualHogarDTO {

    private final Long hogarId;

    //Fechas
    private final LocalDateTime fechaDesde;
    private final LocalDateTime fechaHasta;
    private final LocalDateTime fechaGeneracion = LocalDateTime.now();

    //Consumos
    private int consumoTotal = 0;
    private float consumoPromedio = 0;

    List<ConsumoMensualSectoresDTO> consumosMensualesSector = new ArrayList<>();

    public ConsumoMensualHogarDTO(Long hogarId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.hogarId = hogarId;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public void addConsumoMensualSector(ConsumoMensualSectoresDTO consumoMensualSector) {
        this.consumosMensualesSector.add(consumoMensualSector);
        consumoTotal += consumoMensualSector.getTotalMes();
        consumoPromedio += (consumoMensualSector.getTotalMes() / (float) consumosMensualesSector.size());
    }

    public List<ConsumoMensualSectoresDTO> getConsumosMensualesSector() {
        return consumosMensualesSector;
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

    public Long getHogarId() {
        return hogarId;
    }

    public int getConsumoTotal() {
        return consumoTotal;
    }

    public void setConsumoTotal(int consumoTotal) {
        this.consumoTotal = consumoTotal;
    }

    public float getConsumoPromedio() {
        return consumoPromedio;
    }

    public void setConsumoPromedio(float consumoPromedio) {
        this.consumoPromedio = consumoPromedio;
    }

}
