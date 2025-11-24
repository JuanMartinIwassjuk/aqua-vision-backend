package com.app.aquavision.entities.domain.gamification;

import com.app.aquavision.entities.domain.Hogar;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import jakarta.persistence.Convert;

@Entity
@Table
public class DesafioHogar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "desafio_id", referencedColumnName = "id")
    private Desafio desafio;

    @Column (name = "progreso")
    private int progreso = 0; // 0-100
    
    
    @ManyToOne
    @JoinColumn(name = "hogar_id", referencedColumnName = "id")
    @JsonIgnore
    private Hogar hogar;
    
    @Column(name = "reclamado")
    @Convert(converter = org.hibernate.type.NumericBooleanConverter.class)
    private Boolean reclamado = false;
    

    // getters y setter para ubicarme

    public Desafio getDesafio() { return desafio; }
    public void setDesafio(Desafio desafio) { this.desafio = desafio; }

    public int getProgreso() { return progreso; }
    public void setProgreso(int progreso) { this.progreso = progreso; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    
    public Hogar getHogar() { return hogar; }
    public void setHogar(Hogar hogar) { this.hogar = hogar; }

    public Boolean isReclamado() { return reclamado != null && reclamado; } 
    public void setReclamado(Boolean reclamado) { this.reclamado = reclamado; }
    
}
