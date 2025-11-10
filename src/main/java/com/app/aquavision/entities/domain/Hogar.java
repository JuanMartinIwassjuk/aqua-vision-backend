package com.app.aquavision.entities.domain;

import com.app.aquavision.entities.domain.gamification.*;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.entities.payments.Facturacion;
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
    private Integer miembros = 1;

    @Column
    private String localidad = "";

    @Column
    private String email = "";

    @Column
    private String nombre = "hogar";

    @Enumerated(EnumType.STRING)
    private TipoHogar tipoHogar;

    @Column
    private String direccion = "direccion";

    @Column
    private int ambientes = 1;

    @Column
    private boolean tienePatio = false;

    @Column
    private boolean tienePileta = false;

    @OneToOne
    private Facturacion facturacion;

    @Column
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int rachaDiaria = 0;

    @Column
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private int puntosDisponibles = 0;


    @OneToMany(mappedBy = "hogar", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Notificacion> notificaciones = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "hogar_medallas",
        joinColumns = @JoinColumn(name = "hogar_id"),
        inverseJoinColumns = @JoinColumn(name = "medalla_id")
    )
    private final List<Medalla> medallas = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "hogar_logros",
        joinColumns = @JoinColumn(name = "hogar_id"),
        inverseJoinColumns = @JoinColumn(name = "logro_id")
    )
    private final List<Logro> logros = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<RecompensaHogar> recompensas = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private List<DesafioHogar> desafios = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    private List<Sector> sectores = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn (name = "hogar_id", referencedColumnName = "id")
    private List<PuntosReclamados> puntosReclamados = new ArrayList<>();

    public Hogar() {
        // Constructor por defecto
    }

    public Hogar(int miembros, String localidad, List<Sector> sectores) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.sectores = sectores;
        this.puntosDisponibles=0;
    }

    public Hogar(int miembros, String localidad, String email) {
        this.miembros = miembros;
        this.localidad = localidad;
        this.email = email;
        this.puntosDisponibles=0;
    }

    public int consumoTotalHora(int hora){
        int consumoTotal = 0;
        for (Sector sector : sectores) {
            consumoTotal += sector.totalConsumo(hora);
        }
        return consumoTotal;
    }

    public float consumoTotalFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        float consumoTotal = 0;
        for (Sector sector : sectores) {
            consumoTotal += sector.consumoTotalPorFecha(fechaInicio, fechaFin);
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
            this.puntosDisponibles -= recompensa.getPuntosNecesarios();
            RecompensaHogar recompensaHogar = new RecompensaHogar(recompensa, EstadoRecompensa.DISPONIBLE, LocalDateTime.now().toLocalDate());
            this.agregarRecompensa(recompensaHogar);
        } else {
            throw new IllegalArgumentException("No tienes suficientes puntos para canjear esta recompensa.");
        }
    }

    public boolean puedeCanjearRecompensa(Recompensa recompensa) {
        return this.puntosDisponibles >= recompensa.getPuntosNecesarios();
    }

    public void agregarRecompensa(RecompensaHogar recompensa) {
        this.recompensas.add(recompensa);
    }

    public void sumarPuntosDisponibles(int puntos) {
        this.puntosDisponibles += puntos;
    }

    public int getPuntos() {
        return puntosDisponibles;
    }

    public void setPuntos(int puntosDisponibles) {
        this.puntosDisponibles = puntosDisponibles;
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

    public String getDireccion() {
    return direccion;
    }

    public void getDireccion(String direccion) {
        this.direccion = direccion;
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

    public void agregarNotificacion(Notificacion notificacion) {
        this.notificaciones.add(notificacion);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Logro> getLogros() {
        return logros;
    }

    public List<Medalla> getMedallas() {
        return medallas;
    }

    public TipoHogar getTipoHogar() {
        return tipoHogar;
    }

    public void setTipoHogar(TipoHogar tipoHogar) {
        this.tipoHogar = tipoHogar;
    }

    public int getAmbientes() {
        return ambientes;
    }

    public void setAmbientes(int ambientes) {
        this.ambientes = ambientes;
    }

    public boolean tienePatio() {
        return tienePatio;
    }

    public void setTienePatio(boolean tienePatio) {
        this.tienePatio = tienePatio;
    }

    public boolean tienePileta() {
        return tienePileta;
    }

    public void setTienePileta(boolean tienePileta) {
        this.tienePileta = tienePileta;
    }

    public Facturacion getFacturacion() {
        return facturacion;
    }

    public void setFacturacion(Facturacion facturacion) {
        this.facturacion = facturacion;
    }

    public List<PuntosReclamados> getPuntosReclamados() {
        return puntosReclamados;
    }

    public void setPuntosReclamados(List<PuntosReclamados> puntosReclamados) {
        this.puntosReclamados = puntosReclamados;
    }

    public List<DesafioHogar> getDesafios() {
        return desafios;
    }

    public void setDesafios(List<DesafioHogar> desafios) {
        this.desafios = desafios;
    }
}
