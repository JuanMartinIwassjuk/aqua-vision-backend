package com.app.aquavision.entities.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "medidores")
public class Medidor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numeroSerie;

    @Column
    @Enumerated(EnumType.STRING)
    private EstadoMedidor estado;

    public Medidor() {}

    public Medidor(int numeroSerie) {
        this.numeroSerie = numeroSerie;
        this.estado = EstadoMedidor.UNKNOWN; // Default state
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(int numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public EstadoMedidor getEstado() {
        return estado;
    }

    public void setEstado(EstadoMedidor estado) {
        this.estado = estado;
    }

}
