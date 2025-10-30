package com.app.aquavision.dto.consumos;

import java.time.LocalDateTime;
import java.util.List;

public class ReporteDiarioHogarDTO {

    private Long hogarId;
    private String localidad;
    private int miembros;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    private int consumoTotal;
    private double costoTotal;
    private List<ReporteDiarioSectorDTO> consumosPorSector;

    public ReporteDiarioHogarDTO(Long hogarId, String localidad, int miembros,
                                 LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                 int consumoTotal, double costoTotal,
                                 List<ReporteDiarioSectorDTO> consumosPorSector) {
        this.hogarId = hogarId;
        this.localidad = localidad;
        this.miembros = miembros;
        this.fechaGeneracion = LocalDateTime.now();
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.consumoTotal = consumoTotal;
        this.costoTotal = costoTotal;
        this.consumosPorSector = consumosPorSector;
    }

    // Getters
    public Long getHogarId() { return hogarId; }
    public String getLocalidad() { return localidad; }
    public int getMiembros() { return miembros; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public LocalDateTime getFechaDesde() { return fechaDesde; }
    public LocalDateTime getFechaHasta() { return fechaHasta; }
    public int getConsumoTotal() { return consumoTotal; }
    public double getCostoTotal() { return costoTotal; }
    public List<ReporteDiarioSectorDTO> getConsumosPorSector() { return consumosPorSector; }
}