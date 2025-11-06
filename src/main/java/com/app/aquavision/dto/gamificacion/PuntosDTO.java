package com.app.aquavision.dto.gamificacion;

public class PuntosDTO {
    private Long hogarId;
    private int puntos;

    public PuntosDTO() {
    }

    public PuntosDTO(Long hogarId, int puntos) {
        this.hogarId = hogarId;
        this.puntos = puntos;
    }

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
}
