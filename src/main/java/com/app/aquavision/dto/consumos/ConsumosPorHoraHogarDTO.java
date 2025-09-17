package com.app.aquavision.dto.consumos;

import com.app.aquavision.entities.domain.Hogar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumosPorHoraHogarDTO {

    private final Long hogarId;
    private final LocalDateTime fechaDesde;
    private final LocalDateTime fechaHasta;
    private final LocalDateTime fechaGeneracion = LocalDateTime.now();
    private Float consumoTotal = 0f;
    private final List<ConsumoPorHoraDTO> consumosPorHora = new ArrayList<>();

    public ConsumosPorHoraHogarDTO(Long hogar_id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.hogarId = hogar_id;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public void addConsumoPorHora(ConsumoPorHoraDTO consumo) {
        this.consumosPorHora.add(consumo);
    }

    public List<ConsumoPorHoraDTO> getConsumosPorHora() {
        return consumosPorHora;
    }

    public Long getHogarId() {
        return hogarId;
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

    public Float getConsumoTotal() {
        return consumoTotal;
    }

    public void setConsumoTotal(Float consumoTotal) {
        this.consumoTotal = consumoTotal;
    }

}
