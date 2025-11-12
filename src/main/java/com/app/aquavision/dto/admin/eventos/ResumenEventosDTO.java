package com.app.aquavision.dto.admin.eventos;

public class ResumenEventosDTO {
    private Integer totalEventos;
    private Double totalLitros;
    private Double totalCosto;
    private Integer tagsActivos;

    public ResumenEventosDTO() {}
    public ResumenEventosDTO(Integer totalEventos, Double totalLitros, Double totalCosto, Integer tagsActivos) {
        this.totalEventos = totalEventos; this.totalLitros = totalLitros; this.totalCosto = totalCosto; this.tagsActivos = tagsActivos;
    }
    public Integer getTotalEventos() {
        return totalEventos;
    }
    public void setTotalEventos(Integer totalEventos) {
        this.totalEventos = totalEventos;
    }
    public Double getTotalLitros() {
        return totalLitros;
    }
    public void setTotalLitros(Double totalLitros) {
        this.totalLitros = totalLitros;
    }
    public Double getTotalCosto() {
        return totalCosto;
    }
    public void setTotalCosto(Double totalCosto) {
        this.totalCosto = totalCosto;
    }
    public Integer getTagsActivos() {
        return tagsActivos;
    }
    public void setTagsActivos(Integer tagsActivos) {
        this.tagsActivos = tagsActivos;
    }
    
    

}