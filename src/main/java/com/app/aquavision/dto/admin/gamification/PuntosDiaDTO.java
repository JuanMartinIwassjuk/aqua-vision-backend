package com.app.aquavision.dto.admin.gamification;


public class PuntosDiaDTO {
    private String fecha; // "yyyy-MM-dd"
    private long puntos;

    public PuntosDiaDTO() {}
    public PuntosDiaDTO(String fecha, long puntos) { this.fecha = fecha; this.puntos = puntos; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public long getPuntos() { return puntos; }
    public void setPuntos(long puntos) { this.puntos = puntos; }
}