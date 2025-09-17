package com.app.aquavision.entities.domain.gamification;

import com.app.aquavision.entities.domain.Sector;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "aqua_evento")
public class AquaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column
    private LocalDateTime fechaInicio;

    @Column
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEvento estadoEvento;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "evento_tags",
            joinColumns = @JoinColumn(name = "evento_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<TagEvento> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Sector sector;

    @Column
    private Double litrosConsumidos;

    @Column
    private Double costo;

    public AquaEvento() {
    }

    public AquaEvento(String titulo, String descripcion, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                      EstadoEvento estadoEvento, Sector sector, Double litrosConsumidos, Double costo) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estadoEvento = estadoEvento;
        this.sector = sector;
        this.litrosConsumidos = litrosConsumidos;
        this.costo = costo;
    }

    public Long getId() {
        return id;
    }

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

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public EstadoEvento getEstado() {
        return estadoEvento;
    }

    public void setEstado(EstadoEvento estadoEvento) {
        this.estadoEvento = estadoEvento;
    }

    public List<TagEvento> getTags() {
        return tags;
    }

    public void setTags(List<TagEvento> tags) {
        this.tags = tags;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Double getLitrosConsumidos() {
        return litrosConsumidos;
    }

    public void setLitrosConsumidos(Double litrosConsumidos) {
        this.litrosConsumidos = litrosConsumidos;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }
}
