package com.app.aquavision.dto.admin;

public class ResumenConsumoGlobalDTO {
    
    private Double total;
    private Double media;
    private Double pico;
    private Double costo;

    public ResumenConsumoGlobalDTO() {}

    public ResumenConsumoGlobalDTO(Double total, Double media, Double pico, Double costo) {
        this.total = total; this.media = media; this.pico = pico; this.costo = costo;
    }

    public Double getTotal() {
        return total;
    }
    public void setTotal(Double total) {
        this.total = total;
    }
    public Double getMedia() {
        return media;
    }
    public void setMedia(Double media) {
        this.media = media;
    }
    public Double getPico() {
        return pico;
    }
    public void setPico(Double pico) {
        this.pico = pico;
    }
    public Double getCosto() {
        return costo;
    }
    public void setCosto(Double costo) {
        this.costo = costo;
    }
    
}
