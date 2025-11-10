package com.app.aquavision.entities.domain.gamification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class PuntosReclamados {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column
    int puntos;

    @Column
    LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    Minijuego miniJuego;

    @Column
    String escena;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Minijuego getMiniJuego() {
        return miniJuego;
    }

    public void setMiniJuego(Minijuego miniJuego) {
        this.miniJuego = miniJuego;
    }

    public String getEscena() {
        return escena;
    }

    public void setEscena(String escena) {
        this.escena = escena;
    }
}
