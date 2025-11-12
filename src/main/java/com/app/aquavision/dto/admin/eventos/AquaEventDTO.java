package com.app.aquavision.dto.admin.eventos;

import java.time.LocalDateTime;
import java.util.List;

public class AquaEventDTO {
    private Long id;
    private String titulo;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado;
    private List<EventTagDTO> tags;
    private Integer litrosConsumidos;
    private Double costo;
    private Long hogarId;
    private String localidad;

    public AquaEventDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<EventTagDTO> getTags() {
        return tags;
    }

    public void setTags(List<EventTagDTO> tags) {
        this.tags = tags;
    }

    public Integer getLitrosConsumidos() {
        return litrosConsumidos;
    }

    public void setLitrosConsumidos(Integer litrosConsumidos) {
        this.litrosConsumidos = litrosConsumidos;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public Long getHogarId() {
        return hogarId;
    }

    public void setHogarId(Long hogarId) {
        this.hogarId = hogarId;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }
    
}
