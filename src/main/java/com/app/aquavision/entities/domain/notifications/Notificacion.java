package com.app.aquavision.entities.domain.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table
public class Notificacion { //TODO: Revisar si persistirlas o no

    @Column
    private TipoNotificacion tipo;

    @Column
    private String mensaje;

    @Column
    private String titulo;

    public Notificacion(TipoNotificacion tipo, String titulo, String mensaje) {
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.titulo = titulo;
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

}
