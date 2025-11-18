package com.app.aquavision.entities.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Medicion")
public class Medicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "sector_id")
    @JsonIgnore
    private Sector sector;

    @Column
    //private int flow;
    private Double flow;

    @Column
    private LocalDateTime timestamp;


    public Medicion() {
        // Constructor por defecto
    }

    /*public Medicion(int flow, LocalDateTime timestamp) {
        this.flow = flow;
        this.timestamp = timestamp;
    }*/
    public Medicion(Double flow, LocalDateTime timestamp) {
        this.flow = flow;
        this.timestamp = timestamp;
    }

    public Sector getSector() {
        return sector;
    }
    public void setSector(Sector sector) {
        this.sector = sector;
    }

    //public int getFlow() { return flow; }
    public Double getFlow() { return flow; }
    //public void setFlow(int flow) { this.flow = flow; }
    public void setFlow(Double flow) { this.flow = flow; }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}
