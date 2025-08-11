package com.app.aquavision.dto;

import com.app.aquavision.entities.domain.EstadoConsumo;

public class ProyeccionSectorDTO {
    private Long sectorId;
    private String nombreSector;
    private double consumoActualMes; // Litros actuales en el mes
    private double consumoProyectadoMes; // Proyección de todo el mes
    private String tendencia; // "Creciente", "Decreciente", "Estable"
    private EstadoConsumo estadoConsumo;

    public ProyeccionSectorDTO() {}
    public ProyeccionSectorDTO(Long sectorId, String nombreSector, double consumoActualMes, double consumoProyectadoMes, String tendencia, EstadoConsumo estadoConsumo) {
        this.sectorId = sectorId;
        this.nombreSector = nombreSector;
        this.consumoActualMes = consumoActualMes;
        this.consumoProyectadoMes = consumoProyectadoMes;
        this.tendencia = tendencia;
        this.estadoConsumo = EstadoConsumo.NORMAL;
    }
    // Getters y setters
    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public String getNombreSector() {
        return nombreSector;
    }

    public void setNombreSector(String nombreSector) {
        this.nombreSector = nombreSector;
    }

    public double getConsumoActualMes() {
        return consumoActualMes;
    }

    public void setConsumoActualMes(double consumoActualMes) {
        this.consumoActualMes = consumoActualMes;
    }

    public double getConsumoProyectadoMes() {
        return consumoProyectadoMes;
    }

    public void setConsumoProyectadoMes(double consumoProyectadoMes) {
        this.consumoProyectadoMes = consumoProyectadoMes;
    }

    public String getTendencia() {
        return tendencia;
    }

    public void setTendencia(String tendencia) {
        this.tendencia = tendencia;
    }

    public EstadoConsumo getEstadoConsumo() {
        return estadoConsumo;
    }
    public void setEstadoConsumo(EstadoConsumo estadoConsumo) {
        this.estadoConsumo = estadoConsumo;
    }
}