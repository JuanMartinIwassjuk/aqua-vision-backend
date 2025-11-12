package com.app.aquavision.dto.admin.localidad;

import java.time.LocalDateTime;
import java.util.List;

public class ReporteLocalidadDTO {
    private LocalDateTime fechaGeneracion;
    private String fechaDesde;
    private String fechaHasta;
    private Double totalGlobal;
    private Integer cantidadLocalidades;
    private List<LocalidadSummaryDTO> resumenPorLocalidad;

    public ReporteLocalidadDTO() {}

    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public String getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(String fechaDesde) { this.fechaDesde = fechaDesde; }

    public String getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(String fechaHasta) { this.fechaHasta = fechaHasta; }

    public Double getTotalGlobal() { return totalGlobal; }
    public void setTotalGlobal(Double totalGlobal) { this.totalGlobal = totalGlobal; }

    public Integer getCantidadLocalidades() { return cantidadLocalidades; }
    public void setCantidadLocalidades(Integer cantidadLocalidades) { this.cantidadLocalidades = cantidadLocalidades; }

    public List<LocalidadSummaryDTO> getResumenPorLocalidad() { return resumenPorLocalidad; }
    public void setResumenPorLocalidad(List<LocalidadSummaryDTO> resumenPorLocalidad) { this.resumenPorLocalidad = resumenPorLocalidad; }
}
