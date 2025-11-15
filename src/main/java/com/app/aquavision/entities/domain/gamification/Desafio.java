package com.app.aquavision.entities.domain.gamification;

import com.app.aquavision.entities.domain.Hogar;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table
public class Desafio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column
    private String titulo;

    @Column
    private String descripcion;

    @Column
    private int puntos_recompensa;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getPuntos_recompensa() {
        return puntos_recompensa;
    }

    public void setPuntos_recompensa(int puntos_recompensa) {
        this.puntos_recompensa = puntos_recompensa;
    }

    public Long getId() {
        return id;
    }

    public void actualizarProgreso(DesafioHogar desafioHogar, Hogar hogar){
        //TODO: Implementar correctamente según cada desafio en su clase correspondiente
    }


}
