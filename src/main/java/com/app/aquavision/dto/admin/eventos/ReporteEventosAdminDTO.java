package com.app.aquavision.dto.admin.eventos;

import java.time.LocalDateTime;
import java.util.List;

public class ReporteEventosAdminDTO {
    private LocalDateTime fechaGeneracion;
    private String fechaDesde;
    private String fechaHasta;
    private ResumenEventosDTO resumen;
    private List<AquaEventDTO> eventos;

    public ReporteEventosAdminDTO() {}

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public ResumenEventosDTO getResumen() {
        return resumen;
    }

    public void setResumen(ResumenEventosDTO resumen) {
        this.resumen = resumen;
    }

    public List<AquaEventDTO> getEventos() {
        return eventos;
    }

    public void setEventos(List<AquaEventDTO> eventos) {
        this.eventos = eventos;
    }
    
}