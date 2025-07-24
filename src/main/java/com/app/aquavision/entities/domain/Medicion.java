package com.app.aquavision.entities.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mediciones")
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private int flow;

    @Column
    private LocalDateTime timestamp;

    public Medicion() {
        // Constructor por defecto
    }

    public Medicion(int flow, LocalDateTime timestamp) {
        this.flow = flow;
        this.timestamp = timestamp;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
