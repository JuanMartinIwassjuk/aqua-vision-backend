package com.app.aquavision.entities.domain.gamification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "RecompensaHogar")
public class RecompensaHogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "recompensa_id", referencedColumnName = "id")
    private Recompensa recompensa;

    @Column
    @Enumerated(EnumType.STRING)
    private EstadoRecompensa estado;

    @Column
    private LocalDate fechaDeReclamo;

    public RecompensaHogar() {
        // Constructor por defecto
    }

    public RecompensaHogar(Recompensa recompensa, EstadoRecompensa estado, LocalDate fechaCanjeo) {
        this.recompensa = recompensa;
        this.estado = estado;
        this.fechaDeReclamo = fechaCanjeo;
    }

    public Recompensa getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(Recompensa recompensa) {
        this.recompensa = recompensa;
    }

    public EstadoRecompensa getEstado() {
        return estado;
    }

    public void setEstado(EstadoRecompensa estado) {
        this.estado = estado;
    }

    public LocalDate getFechaCanjeo() {
        return fechaDeReclamo;
    }

    public void setFechaCanjeo(LocalDate fechaCanjeo) {
        this.fechaDeReclamo = fechaCanjeo;
    }

}
