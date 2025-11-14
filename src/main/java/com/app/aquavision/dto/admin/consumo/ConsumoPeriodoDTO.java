package com.app.aquavision.dto.admin.consumo;

public class ConsumoPeriodoDTO {

    private String fecha;
    private Double totalLitros;
    private Double costo;

    public ConsumoPeriodoDTO() {}

    public ConsumoPeriodoDTO(String fecha, Double totalLitros, Double costo) {
        this.fecha = fecha;
        this.totalLitros = totalLitros;
        this.costo = costo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double getTotalLitros() {
        return totalLitros;
    }

    public void setTotalLitros(Double totalLitros) {
        this.totalLitros = totalLitros;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }
}
