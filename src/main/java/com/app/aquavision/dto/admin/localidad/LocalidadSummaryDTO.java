package com.app.aquavision.dto.admin.localidad;

public class LocalidadSummaryDTO {
    private String localidad;
    private Double total;
    private Double media;
    private Double costo;
    private Integer hogares;

    public LocalidadSummaryDTO() {}

    public LocalidadSummaryDTO(String localidad, Double total, Double media, Double costo, Integer hogares) {
        this.localidad = localidad;
        this.total = total;
        this.media = media;
        this.costo = costo;
        this.hogares = hogares;
    }


    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }

    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }

    public Double getMedia() { return media; }
    public void setMedia(Double media) { this.media = media; }

    public Double getCosto() { return costo; }
    public void setCosto(Double costo) { this.costo = costo; }

    public Integer getHogares() { return hogares; }
    public void setHogares(Integer hogares) { this.hogares = hogares; }
}
