package com.app.aquavision.dto.admin.dashboard;

public class ReportAdminDashboardDTO {
    private Double consumoPromHoy;
    private Double consumoPromAyer;
    private Long trivias; 
    private Long eventos; 


    public ReportAdminDashboardDTO(Double consumoPromHoy, Double consumoPromAyer, Long trivias, Long eventos) {
        this.consumoPromHoy = consumoPromHoy;
        this.consumoPromAyer = consumoPromAyer;
        this.trivias = trivias;
        this.eventos = eventos;
    }

    public ReportAdminDashboardDTO() {}

    public Double getConsumoPromHoy() { return consumoPromHoy; }
    public void setConsumoPromHoy(Double consumoPromHoy) { this.consumoPromHoy = consumoPromHoy; }
    public Double getConsumoPromAyer() { return consumoPromAyer; }
    public void setConsumoPromAyer(Double consumoPromAyer) { this.consumoPromAyer = consumoPromAyer; }
    public Long getTrivias() { return trivias; }
    public void setTrivias(Long trivias) { this.trivias = trivias; }
    public Long getEventos() { return eventos; }
    public void setEventos(Long eventos) { this.eventos = eventos; }
}
