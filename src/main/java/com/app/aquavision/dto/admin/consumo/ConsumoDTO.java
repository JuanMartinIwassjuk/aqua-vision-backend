package com.app.aquavision.dto.admin.consumo;

public class ConsumoDTO {

    private java.time.LocalDateTime fecha;
    private Double litros;

    public ConsumoDTO() {}

    public ConsumoDTO(java.time.LocalDateTime fecha, Double litros) {
        this.fecha = fecha;
        this.litros = litros;
    }

    public java.time.LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(java.time.LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getLitros() {
        return litros;
    }

    public void setLitros(Double litros) {
        this.litros = litros;
    }
}
