package com.app.aquavision.dto.admin.gamification;

import java.util.List;

public class MedallasHogarDTO {
    private Long hogarId;
    private String hogarNombre;
    private List<String> medallas;

    public MedallasHogarDTO() {}
    public MedallasHogarDTO(Long hogarId, String hogarNombre, List<String> medallas) {
        this.hogarId = hogarId; this.hogarNombre = hogarNombre; this.medallas = medallas;
    }
    public Long getHogarId() { return hogarId; }
    public void setHogarId(Long hogarId) { this.hogarId = hogarId; }
    public String getHogarNombre() { return hogarNombre; }
    public void setHogarNombre(String hogarNombre) { this.hogarNombre = hogarNombre; }
    public List<String> getMedallas() { return medallas; }
    public void setMedallas(List<String> medallas) { this.medallas = medallas; }
}