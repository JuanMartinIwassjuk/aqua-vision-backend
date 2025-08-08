package com.app.aquavision.entities.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Sector")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nombre;
    @Column
    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn (name = "sector_id", referencedColumnName = "id")
    @JsonIgnore
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

    public int consumoTotalPorFecha(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        int consumoTotal = 0;

        if (!mediciones.isEmpty()) {
            for (Medicion medicion : mediciones) {
                if (medicion.getTimestamp().isAfter(fechaInicio) && medicion.getTimestamp().isBefore(fechaFin)) {
                    consumoTotal += medicion.getFlow();
                }
            }
        }

        return consumoTotal;
    }

    public int consumoActualDiaro() {
        int consumoActual = 0;
        LocalDateTime hoy = LocalDateTime.now();

        if (!mediciones.isEmpty()) {
            for (Medicion medicion : mediciones) {
                if (medicion.getTimestamp().toLocalDate().equals(hoy.toLocalDate())) {
                    consumoActual += medicion.getFlow();
                }
            }
        }

        return consumoActual;
    }

    public int totalConsumo() {
        int totalConsumo = 0;

        if (!mediciones.isEmpty()) {
            for (Medicion medicion : mediciones) {
                totalConsumo += medicion.getFlow();
            }
        }

        return totalConsumo;
    }

    public int promedioConsumo() {

        int totalConsumo = totalConsumo();

        if (mediciones.isEmpty()) {
            return 0; // Evitar división por cero
        }

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

    public Long getId() {
        return id;
    }
}
