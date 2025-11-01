package com.app.aquavision.entities.payments;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table
public class Facturacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MedioDePago medioDePago;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    public Facturacion(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MedioDePago getMedioDePago() {
        return medioDePago;
    }

    public void setMedioDePago(MedioDePago medioDePago) {
        this.medioDePago = medioDePago;
    }

    public Plan getTipoPlan() {
        return plan;
    }

    public void setTipoPlan(Plan plan) {
        this.plan = plan;
    }
}
