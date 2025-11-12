package com.app.aquavision.dto.admin.consumo;


import java.time.LocalDateTime;
import java.util.List;

public class ReporteConsumoAdminDTO {
    private LocalDateTime fechaGeneracion;
    private String fechaDesde;
    private String fechaHasta;
    private ResumenConsumoGlobalDTO resumen;
    private List<HogarConsumoDTO> hogares;

    public ReporteConsumoAdminDTO() {}
    public ReporteConsumoAdminDTO(LocalDateTime fechaGeneracion, String fechaDesde, String fechaHasta,
                                  ResumenConsumoGlobalDTO resumen, List<HogarConsumoDTO> hogares) {
        this.fechaGeneracion = fechaGeneracion;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.resumen = resumen;
        this.hogares = hogares;
    }
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
    public ResumenConsumoGlobalDTO getResumen() {
        return resumen;
    }
    public void setResumen(ResumenConsumoGlobalDTO resumen) {
        this.resumen = resumen;
    }
    public List<HogarConsumoDTO> getHogares() {
        return hogares;
    }
    public void setHogares(List<HogarConsumoDTO> hogares) {
        this.hogares = hogares;
    }
    
}
