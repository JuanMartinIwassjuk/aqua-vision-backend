package com.app.aquavision.dto.admin.gamification;


public class HogarRankingDTO {
    private Long id;
    private String nombre;
    private long puntos;
    private double puntajeRanking;
    private int racha;

    public HogarRankingDTO() {}
    public HogarRankingDTO(Long id, String nombre, long puntos, double puntajeRanking, int racha) {
        this.id = id; this.nombre = nombre; this.puntos = puntos; this.puntajeRanking = puntajeRanking; this.racha = racha;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public long getPuntos() { return puntos; }
    public void setPuntos(long puntos) { this.puntos = puntos; }
    public double getPuntajeRanking() { return puntajeRanking; }
    public void setPuntajeRanking(double puntajeRanking) { this.puntajeRanking = puntajeRanking; }
    public int getRacha() { return racha; }
    public void setRacha(int racha) { this.racha = racha; }
}
