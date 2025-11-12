package com.app.aquavision.dto.admin.consumo;

public class HogarConsumoDTO {
    private Long hogarId;
    private String nombre;
    private String localidad;
    private Integer miembros;
    private Double consumoTotal; 
    private Double costo; 

    public HogarConsumoDTO() {}
    public HogarConsumoDTO(Long hogarId, String nombre, String localidad, Integer miembros, Double consumoTotal, Double costo) {
        this.hogarId = hogarId; this.nombre = nombre; this.localidad = localidad; this.miembros = miembros;
        this.consumoTotal = consumoTotal; this.costo = costo;
    }
    public Long getHogarId() {
        return hogarId;
    }
    public void setHogarId(Long hogarId) {
        this.hogarId = hogarId;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getLocalidad() {
        return localidad;
    }
    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }
    public Integer getMiembros() {
        return miembros;
    }
    public void setMiembros(Integer miembros) {
        this.miembros = miembros;
    }
    public Double getConsumoTotal() {
        return consumoTotal;
    }
    public void setConsumoTotal(Double consumoTotal) {
        this.consumoTotal = consumoTotal;
    }
    public Double getCosto() {
        return costo;
    }
    public void setCosto(Double costo) {
        this.costo = costo;
    }
    
}
