package com.app.aquavision.dto.admin.gamification;


public class ResumenGamificacionDTO {
    private long total;
    private double media;
    private int mejorRacha;

    public ResumenGamificacionDTO() {}
    public ResumenGamificacionDTO(long total, double media, int mejorRacha) {
        this.total = total; this.media = media; this.mejorRacha = mejorRacha;
    }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public double getMedia() { return media; }
    public void setMedia(double media) { this.media = media; }
    public int getMejorRacha() { return mejorRacha; }
    public void setMejorRacha(int mejorRacha) { this.mejorRacha = mejorRacha; }
}
