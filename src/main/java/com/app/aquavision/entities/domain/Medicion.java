package com.app.aquavision.entities.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mediciones")
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sector_id")
    private Sector sector;

    @Column
    private int flow;

    @Column
    @JsonFormat(pattern = "yyyy:MM:dd:HH:mm")
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
