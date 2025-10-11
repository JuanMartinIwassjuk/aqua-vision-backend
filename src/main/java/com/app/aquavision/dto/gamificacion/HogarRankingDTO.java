package com.app.aquavision.dto.gamificacion;

public class HogarRankingDTO {

    public String nombre;
    public int puntaje_ranking;
    public int posicion;

    public HogarRankingDTO() {
    }

    public HogarRankingDTO(String nombre, int puntaje_ranking, int posicion) {
        this.nombre = nombre;
        this.puntaje_ranking = puntaje_ranking;
        this.posicion = posicion;
    }


}
