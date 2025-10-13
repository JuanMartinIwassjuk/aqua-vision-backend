package com.app.aquavision.entities.domain.notifications;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;

import com.app.aquavision.entities.domain.Hogar;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TipoNotificacion tipo;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private String titulo;

    @Column
    private boolean leido = false;

    @Column
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hogar_id", nullable = false)
    @JsonBackReference
    private Hogar hogar;

    public Notificacion(TipoNotificacion tipo, String titulo, String mensaje) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.titulo = titulo;
    }

    public Notificacion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    public void leer() {
        this.setLeido(true);
    }

    public Hogar getHogar() {
        return hogar;
    }

    public void setHogar(Hogar hogar) {
        this.hogar = hogar;
    }

}
