package com.app.aquavision.dto;

import java.util.List;

public class ProyeccionHogarDTO {
    private Long hogarId;
    private List<ProyeccionSectorDTO> sectores;

    // Getters y setters
    public Long getHogarId() {
        return hogarId;
    }

    public void setHogarId(Long hogarId) {
        this.hogarId = hogarId;
    }

    public List<ProyeccionSectorDTO> getSectores() {
        return sectores;
    }

    public void setSectores(List<ProyeccionSectorDTO> sectores) {
        this.sectores = sectores;
    }
}