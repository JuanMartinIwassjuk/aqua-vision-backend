package com.app.aquavision.dto.gamificacion;

import com.app.aquavision.entities.domain.gamification.DesafioHogar;

import java.util.ArrayList;
import java.util.List;

public class DesafiosHogarDTO {

    Long hogarId;

    List<DesafioHogar> desafiosHogar = new ArrayList<>();

    public DesafiosHogarDTO() {
    }

    public DesafiosHogarDTO(Long hogarId, List<DesafioHogar> desafiosHogar) {
        this.hogarId = hogarId;
        this.desafiosHogar = desafiosHogar;
    }

    public List<DesafioHogar> getDesafisoHogar() {
        return desafiosHogar;
    }

    public void setDesafisoHogar(List<DesafioHogar> desafiosHogar) {
        this.desafiosHogar = desafiosHogar;
    }

    public Long getHogarId() {
        return hogarId;
    }

    public void setHogarId(Long hogarId) {
        this.hogarId = hogarId;
    }

}
