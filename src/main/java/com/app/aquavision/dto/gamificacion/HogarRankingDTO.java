package com.app.aquavision.dto.gamificacion;

public class HogarRankingDTO {

    public String nombre;
    public String puntaje_ranking;
    public int posicion;
    public String juego_mas_jugado;
    public String localidad;
    public int cantidad_sectores;

    public HogarRankingDTO() {
    }

    public HogarRankingDTO(String nombre, String puntaje_ranking, int posicion, String juego_mas_jugado) {
        this.nombre = nombre;
        this.puntaje_ranking = puntaje_ranking;
        this.posicion = posicion;
        this.juego_mas_jugado = juego_mas_jugado;
    }


    public HogarRankingDTO(
            String nombre,
            String puntos,
            int posicion,
            String juego_mas_jugado,
            String localidad,
            int cantidad_sectores
    ) {
        this(nombre, puntos, posicion, juego_mas_jugado);
        this.localidad = localidad;
        this.cantidad_sectores = cantidad_sectores;
    }
}
