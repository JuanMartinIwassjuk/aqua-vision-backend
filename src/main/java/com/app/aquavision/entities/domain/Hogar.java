package com.app.aquavision.entities.domain;

import com.app.aquavision.entities.domain.gamification.EstadoRecompensa;
import com.app.aquavision.entities.domain.gamification.Recompensa;
import com.app.aquavision.entities.domain.gamification.RecompensaHogar;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Hogar")
public class Hogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column
    private int miembros = 1;

    @Column
    private String localidad = "";

    @Column
    private String email = "";

    @Column
    private String nombre = "hogar";

    @Column
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int rachaDiaria = 0;

    @Column
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int puntos = 0;

    @Column()
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int puntaje_ranking = 0;
    @OneToMany(mappedBy = "hogar", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Notificacion> notificaciones = new ArrayList<>();




    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<RecompensaHogar> recompensas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    private List<Sector> sectores = new ArrayList<>();

    public Hogar() {
        // Constructor por defecto
    }

    public Hogar(int miembros, String localidad, List<Sector> sectores) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.sectores = sectores;
        this.puntos=0;
    }

    public Hogar(int miembros, String localidad, String email) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.email = email;
        this.puntos=0;
    }

    public int consumoTotalHora(int hora){
        int consumoTotal = 0;
        for (Sector sector : sectores) {
            consumoTotal += sector.totalConsumo(hora);
        }
        return consumoTotal;
    }

    public void aumentarRachaDiaria() {
        this.rachaDiaria += 1;
    }

    public void resetearRachaDiaria() {
        this.rachaDiaria = 0;
    }

    public void reclamarRecompensa(Recompensa recompensa) {
        if (this.puedeCanjearRecompensa(recompensa)) {
            this.puntos -= recompensa.getPuntosNecesarios();
            RecompensaHogar recompensaHogar = new RecompensaHogar(recompensa, EstadoRecompensa.DISPONIBLE, LocalDateTime.now().toLocalDate());
            this.agregarRecompensa(recompensaHogar);
        } else {
            throw new IllegalArgumentException("No tienes suficientes puntos para canjear esta recompensa.");
        }
    }

    public boolean puedeCanjearRecompensa(Recompensa recompensa) {
        return this.puntos >= recompensa.getPuntosNecesarios();
    }

    public void agregarRecompensa(RecompensaHogar recompensa) {
        this.recompensas.add(recompensa);
    }

    public void sumarPuntos(int puntos) {
        this.puntaje_ranking += puntos;
        this.puntos += puntos;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    public List<RecompensaHogar> getRecompensas() {
        return recompensas;
    }

    public void setRecompensas(List<RecompensaHogar> recompensas) {
        this.recompensas = recompensas;
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

    public Long getId() {
        return id;
    }

    public void setRachaDiaria(int rachaDiaria) {
        this.rachaDiaria = rachaDiaria;
    }

    public int getRachaDiaria() {
        return rachaDiaria;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Notificacion> getNotificaciones() {
        return notificaciones;
    }

    public void setNotificaciones(List<Notificacion> notificaciones) {
        this.notificaciones = notificaciones;
    }

    public int getPuntaje_ranking() {
        return puntaje_ranking;
    }

    public void setPuntaje_ranking(int puntaje_ranking) {
        this.puntaje_ranking = puntaje_ranking;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
