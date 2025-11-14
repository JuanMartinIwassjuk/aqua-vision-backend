package com.app.aquavision.dto.admin.consumo;


public class TopMesDTO {

    private int mes;
    private double total;
    private double promedio;

    public TopMesDTO() {}

    public TopMesDTO(int mes, double total, double promedio) {
        this.mes = mes;
        this.total = total;
        this.promedio = promedio;
    }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getPromedio() { return promedio; }
    public void setPromedio(double promedio) { this.promedio = promedio; }
}
