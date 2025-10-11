package com.app.aquavision.dto.consumos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumosPorHoraSectoresDTO {

    private Long hogarId;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private final LocalDateTime fechaGeneracion = LocalDateTime.now();
    private Float consumoTotal = 0f;
    private final List<ConsumosPorHoraSectorDTO> consumosPorHora = new ArrayList<>();

    public ConsumosPorHoraSectoresDTO(){}

    public ConsumosPorHoraSectoresDTO(Long hogar_id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        this.hogarId = hogar_id;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }

    public void addConsumoPorHora(ConsumosPorHoraSectorDTO consumo) {
        this.consumosPorHora.add(consumo);
    }

    public List<ConsumosPorHoraSectorDTO> getConsumosPorHora() {
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
