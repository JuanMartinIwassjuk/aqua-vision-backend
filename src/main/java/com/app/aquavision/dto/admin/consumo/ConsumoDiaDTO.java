package com.app.aquavision.dto.admin.consumo;


public class ConsumoDiaDTO {

    private java.time.LocalDate fecha;
    private Double consumoTotal;
    private Double consumoPromedio;
    private Double consumoMaximo;

    public ConsumoDiaDTO() {}

    public ConsumoDiaDTO(java.time.LocalDate fecha, Double total, Double promedio, Double maximo) {
        this.fecha = fecha;
        this.consumoTotal = total;
        this.consumoPromedio = promedio;
        this.consumoMaximo = maximo;
    }

    public java.time.LocalDate getFecha() { return fecha; }
    public void setFecha(java.time.LocalDate fecha) { this.fecha = fecha; }

    public Double getConsumoTotal() { return consumoTotal; }
    public void setConsumoTotal(Double consumoTotal) { this.consumoTotal = consumoTotal; }

    public Double getConsumoPromedio() { return consumoPromedio; }
    public void setConsumoPromedio(Double consumoPromedio) { this.consumoPromedio = consumoPromedio; }

    public Double getConsumoMaximo() { return consumoMaximo; }
    public void setConsumoMaximo(Double consumoMaximo) { this.consumoMaximo = consumoMaximo; }
}
