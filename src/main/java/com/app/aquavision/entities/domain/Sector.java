package com.app.aquavision.entities.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Sector")
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column
    private String nombre;

    @Column(name = "umbral_mensual")
    private float umbralMensual = 0;

    @Column
    @Enumerated(EnumType.STRING)
    private CategoriaSector categoriaSector;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn (name = "sector_id", referencedColumnName = "id")
    @JsonIgnore
    private List<Medicion> mediciones = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "medidor_id")
    private Medidor medidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hogar_id")
    @JsonIgnore
    private Hogar hogar;


    public Sector() {
        // Constructor por defecto
    }

    public Sector(String nombre, CategoriaSector categoriaSector, Medidor medidor) {
        this.nombre = nombre;
        this.categoriaSector = categoriaSector;
        this.medidor = medidor;
    }
    public Hogar getHogar() {
        return hogar;
    }

    public void setHogar(Hogar hogar) {
        this.hogar = hogar;
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

    public int totalConsumo(int hora){
        int totalConsumoHora = 0;

        if (!mediciones.isEmpty()) {
            for (Medicion medicion : mediciones) {
                if (medicion.getTimestamp().getHour() == hora) {
                    totalConsumoHora += medicion.getFlow();
                }
            }
        }

        return totalConsumoHora;
    }

    public int promedioConsumo() {

        int totalConsumo = totalConsumo();

        if (mediciones.isEmpty()) {
            return 0; // Evitar división por cero
        }

        return totalConsumo / mediciones.size();
    }

    public int picoConsumo() {
        int picoConsumo = 0;

        if (!mediciones.isEmpty()) {
            for (Medicion medicion : mediciones) {
                if (medicion.getFlow() > picoConsumo) {
                    picoConsumo = medicion.getFlow();
                }
            }
        }

        return picoConsumo;
    }

    public float getProyeccionHasta(LocalDateTime hasta){
        LocalDateTime hoy = LocalDateTime.now();
        int diferenciaDeDias = hasta.getDayOfMonth() - hoy.getDayOfMonth();

        return diferenciaDeDias * this.promedioConsumo(); //TODO: Ver de usar una proyeccion mas precisa
    }

    public boolean tienePerdidaAguaReciente() { //TODO: Ver de validarlo con mayor precision
        // Una pérdida de agua se define como un consumo continuo de 1hora o más

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime haceUnaHora = ahora.minusHours(1);

        int consumoTotalHora = consumoTotalPorFecha(haceUnaHora, ahora);

        return consumoTotalHora > this.getUmbralMensual() / 720; // Suponiendo 720 horas en un mes
    }

    public CategoriaSector getCategoria() {
        return categoriaSector;
    }

    public void setCategoria(CategoriaSector categoriaSector) {
        this.categoriaSector = categoriaSector;
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

    public Medidor getMedidor() {
        return medidor;
    }

    public void setMedidor(Medidor medidor) {
        this.medidor = medidor;
    }

    public float getUmbralMensual() {
        return umbralMensual;
    }

    public void setUmbralMensual(float umbralMensual) {
        this.umbralMensual = umbralMensual;
    }

}
