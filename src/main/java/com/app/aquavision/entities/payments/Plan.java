package com.app.aquavision.entities.payments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoPlan tipoPlan;

    @Column
    private float costoMensual;

    public TipoPlan getPlan() {
        return tipoPlan;
    }

    public void setPlan(TipoPlan tipoPlan) {
        this.tipoPlan = tipoPlan;
    }

    public float getCostoMensual() {
        return costoMensual;
    }

    public void setCostoMensual(float costoMensual) {
        this.costoMensual = costoMensual;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
