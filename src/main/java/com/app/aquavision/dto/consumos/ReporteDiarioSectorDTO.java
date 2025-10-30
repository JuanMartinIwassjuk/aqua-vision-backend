package com.app.aquavision.dto.consumos;

public class ReporteDiarioSectorDTO {

    private String nombreSector;
    private int consumoTotal;
    private float mediaConsumo;
    private float picoMaximo;
    private double costo;

    public ReporteDiarioSectorDTO(String nombreSector, int consumoTotal, float mediaConsumo, float picoMaximo, double costo) {
        this.nombreSector = nombreSector;
        this.consumoTotal = consumoTotal;
        this.mediaConsumo = mediaConsumo;
        this.picoMaximo = picoMaximo;
        this.costo = costo;
    }

    // Getters
    public String getNombreSector() { return nombreSector; }
    public int getConsumoTotal() { return consumoTotal; }
    public float getMediaConsumo() { return mediaConsumo; }
    public float getPicoMaximo() { return picoMaximo; }
    public double getCosto() { return costo; }

    // Setters (opcional)
    public void setNombreSector(String nombreSector) { this.nombreSector = nombreSector; }
    public void setConsumoTotal(int consumoTotal) { this.consumoTotal = consumoTotal; }
    public void setMediaConsumo(float mediaConsumo) { this.mediaConsumo = mediaConsumo; }
    public void setPicoMaximo(float picoMaximo) { this.picoMaximo = picoMaximo; }
    public void setCosto(double costo) { this.costo = costo; }
}
