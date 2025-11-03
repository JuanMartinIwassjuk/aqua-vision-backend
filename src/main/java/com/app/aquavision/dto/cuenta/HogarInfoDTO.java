package com.app.aquavision.dto.cuenta;

import com.app.aquavision.entities.domain.TipoHogar;

public class HogarInfoDTO {
    private String nombreHogar;
    private String direccion;
    private String ciudad;
    private TipoHogar tipoHogar;
    private int cantidadBanos;
    private int cantidadIntegrantes;
    private boolean tienePatio;
    private boolean tienePileta;
    private boolean tieneTanquePropio;
    private boolean tieneTermotanqueElectrico;

    public HogarInfoDTO() {
    }

    // Getters & setters
    public String getNombreHogar() {
        return nombreHogar;
    }

    public HogarInfoDTO setNombreHogar(String nombreHogar) {
        this.nombreHogar = nombreHogar;
        return this;
    }

    public String getDireccion() {
        return direccion;
    }

    public HogarInfoDTO setDireccion(String direccion) {
        this.direccion = direccion;
        return this;
    }

    public String getCiudad() {
        return ciudad;
    }

    public HogarInfoDTO setCiudad(String ciudad) {
        this.ciudad = ciudad;
        return this;
    }

    public TipoHogar getTipoHogar() {
        return tipoHogar;
    }

    public HogarInfoDTO setTipoHogar(TipoHogar tipoHogar) {
        this.tipoHogar = tipoHogar;
        return this;
    }

    public int getCantidadBanos() {
        return cantidadBanos;
    }

    public HogarInfoDTO setCantidadBanos(int cantidadBanos) {
        this.cantidadBanos = cantidadBanos;
        return this;
    }

    public int getCantidadIntegrantes() {
        return cantidadIntegrantes;
    }

    public HogarInfoDTO setCantidadIntegrantes(int cantidadIntegrantes) {
        this.cantidadIntegrantes = cantidadIntegrantes;
        return this;
    }

    public boolean isTienePatio() {
        return tienePatio;
    }

    public HogarInfoDTO setTienePatio(boolean tienePatio) {
        this.tienePatio = tienePatio;
        return this;
    }

    public boolean isTienePileta() {
        return tienePileta;
    }

    public HogarInfoDTO setTienePileta(boolean tienePileta) {
        this.tienePileta = tienePileta;
        return this;
    }

    public boolean isTieneTanquePropio() {
        return tieneTanquePropio;
    }

    public HogarInfoDTO setTieneTanquePropio(boolean tieneTanquePropio) {
        this.tieneTanquePropio = tieneTanquePropio;
        return this;
    }

    public boolean isTieneTermotanqueElectrico() {
        return tieneTermotanqueElectrico;
    }

    public HogarInfoDTO setTieneTermotanqueElectrico(boolean tieneTermotanqueElectrico) {
        this.tieneTermotanqueElectrico = tieneTermotanqueElectrico;
        return this;
    }
}
