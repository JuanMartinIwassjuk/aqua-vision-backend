package com.app.aquavision.dto.proyecciones;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProyeccionPuntosDTO {

    private Integer dia;
    private Double consumoHistorico;
    private Double consumoActual;
    private Double consumoProyectado;
    private Double tendenciaMin;
    private Double tendenciaMax;

    public ProyeccionPuntosDTO(Integer dia, Double consumoHistorico, Double consumoActual, Double consumoProyectado, Double tendenciaMin, Double tendenciaMax) {
        this.dia = dia;
        this.consumoHistorico = consumoHistorico;
        this.consumoActual = consumoActual;
        this.consumoProyectado = consumoProyectado;
        this.tendenciaMin = tendenciaMin;
        this.tendenciaMax = tendenciaMax;
    }

    // Getters y Setters
    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Double getConsumoHistorico() {
        return consumoHistorico;
    }

    public void setConsumoHistorico(Double consumoHistorico) {
        this.consumoHistorico = consumoHistorico;
    }

    public Double getConsumoActual() {
        return consumoActual;
    }

    public void setConsumoActual(Double consumoActual) {
        this.consumoActual = consumoActual;
    }

    public Double getConsumoProyectado() {
        return consumoProyectado;
    }

    public void setConsumoProyectado(Double consumoProyectado) {
        this.consumoProyectado = consumoProyectado;
    }

    public Double getTendenciaMin() {
        return tendenciaMin;
    }

    public void setTendenciaMin(Double tendenciaMin) {
        this.tendenciaMin = tendenciaMin;
    }

    public Double getTendenciaMax() {
        return tendenciaMax;
    }

    public void setTendenciaMax(Double tendenciaMax) {
        this.tendenciaMax = tendenciaMax;
    }
}