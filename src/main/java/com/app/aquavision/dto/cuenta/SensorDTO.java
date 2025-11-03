package com.app.aquavision.dto.cuenta;

import com.app.aquavision.entities.domain.EstadoMedidor;

import java.time.LocalDateTime;

public class SensorDTO {
    private String nombreSensor;
    private EstadoMedidor estadoActual;
    private LocalDateTime ultimaMedicion;
    private int consumoActual;
    public SensorDTO() {
    }
    // Getters & setters
    public String getNombreSensor() {
        return nombreSensor;
    }

    public SensorDTO setNombreSensor(String nombreSensor) {
        this.nombreSensor = nombreSensor;
        return this;
    }

    public EstadoMedidor getEstadoActual() {
        return estadoActual;
    }

    public SensorDTO setEstadoActual(EstadoMedidor estadoActual) {
        this.estadoActual = estadoActual;
        return this;
    }

    public LocalDateTime getUltimaMedicion() {
        return ultimaMedicion;
    }

    public SensorDTO setUltimaMedicion(LocalDateTime ultimaMedicion) {
        this.ultimaMedicion = ultimaMedicion;
        return this;
    }

    public int getConsumoActual() {
        return consumoActual;
    }

    public SensorDTO setConsumoActual(int consumoActual) {
        this.consumoActual = consumoActual;
        return this;
    }
}
