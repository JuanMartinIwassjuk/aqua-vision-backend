package com.app.aquavision.entities.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hogares")
public class Hogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int miembros = 1;

    @Column
    private String localidad = "";

    @OneToMany()
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    private List<Sector> sectores = new ArrayList<>();

    public Hogar() {
        // Constructor por defecto
    }

    public Hogar(int miembros, String localidad, List<Sector> sectores) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.sectores = sectores;
    }

    public Hogar(int miembros, String localidad) {
        this.miembros = miembros;
        this.localidad = localidad;
    }

    public void mostrarReporteHogar(){

        System.out.println("Reporte del Hogar:");
        System.out.println(" -Miembros: " + miembros);
        System.out.println(" -Localidad: " + localidad);
        System.out.println(" -Sectores: " + sectores.size());

        int totalConsumoHogar = 0;

        for (Sector sector : sectores) {
            int totalConsumoSector = sector.getTotalConsumo();
            int promedioConsumoSector = sector.getPromedioConsumo();

            System.out.println("  Sector: " + sector.getNombre());
            System.out.println("   -Categoria: " + sector.getCategoria());
            System.out.println("   -Total Consumo: " + totalConsumoSector);
            System.out.println("   -Promedio Consumo: " + promedioConsumoSector);

            totalConsumoHogar += totalConsumoSector;
        }

        System.out.println(" -Total Consumo Hogar: " + totalConsumoHogar);
        System.out.println(" -Promedio Consumo x Sector: " + totalConsumoHogar / sectores.size());
    }

    public List<Sector> getSectores() {
        return sectores;
    }

    public void setSectores(List<Sector> sectores) {
        this.sectores = sectores;
    }

    public int getMiembros() {
        return miembros;
    }

    public void setMiembros(int miembros) {
        this.miembros = miembros;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

}
