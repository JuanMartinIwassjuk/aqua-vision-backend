package com.app.aquavision.dto.consumos;

public class ConsumoPorHoraDTO {

    private final int hora;
    private final int consumo;


    public ConsumoPorHoraDTO(int hora, int consumo) {
        this.hora = hora;
        this.consumo = consumo;
    }

    public int getHora() {
        return hora;
    }

    public int getConsumo() {
        return consumo;
    }
}
