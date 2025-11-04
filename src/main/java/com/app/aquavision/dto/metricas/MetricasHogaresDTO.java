package com.app.aquavision.dto.metricas;

import com.app.aquavision.entities.domain.TipoHogar;

import java.time.LocalDateTime;

public class MetricasHogaresDTO {

    public LocalDateTime fechaGeneracion = LocalDateTime.now();
    public LocalDateTime fechaDesde;
    public LocalDateTime fechaHasta;
    public String localidad;
    public Integer miembros;
    public TipoHogar tipoHogar;
    public int totalHogares;
    public float consumoTotal;
    public float consumoPromedio;

    public MetricasHogaresDTO() {
    }

    public MetricasHogaresDTO(String localidad, int miembros, TipoHogar tipoHogar, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.localidad = localidad;
        this.miembros = miembros;
        this.tipoHogar = tipoHogar;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public void setTotalHogares(int totalHogares) {
        this.totalHogares = totalHogares;
    }

    public void setConsumoTotal(float consumoTotal) {
        this.consumoTotal = consumoTotal;
    }

    public void setConsumoPromedio(float consumoPromedio) {
        this.consumoPromedio = consumoPromedio;
    }
}
