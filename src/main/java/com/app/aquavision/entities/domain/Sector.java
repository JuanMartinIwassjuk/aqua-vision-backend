package com.app.aquavision.entities.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sectores")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nombre;
    @Column
    private Categoria categoria;

    @OneToMany
    @JoinColumn (name = "sector_id", referencedColumnName = "id")
    private List<Medicion> mediciones = new ArrayList<>();

    public Sector() {
        // Constructor por defecto
    }

    public Sector(String nombre, Categoria categoria) {
        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTotalConsumo() {
        int totalConsumo = 0;

        if (!mediciones.isEmpty()) {
            for (Medicion medicion : mediciones) {
                totalConsumo += medicion.getFlow();
            }
        }

        return totalConsumo;
    }

    public int getPromedioConsumo() {

        int totalConsumo = getTotalConsumo();
        return totalConsumo / mediciones.size();
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Medicion> getMediciones() {
        return mediciones;
    }

    public void setMediciones(List<Medicion> mediciones) {
        this.mediciones = mediciones;
    }
}
