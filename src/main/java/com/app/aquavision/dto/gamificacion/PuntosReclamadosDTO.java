package com.app.aquavision.dto.gamificacion;

import java.time.LocalDateTime;

public class PuntosReclamadosDTO {

    private Long hogarId;
    private int puntos;
    private LocalDateTime fecha;
    private String minijuego;    //Revisar


    public Long getHogarId() {
        return hogarId;
    }

    public void setHogarId(Long hogarId) {
        this.hogarId = hogarId;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getMinijuego() {
        return minijuego;
    }

    public void setMinijuego(String minujuego) {
        this.minijuego = minujuego;
    }
}
