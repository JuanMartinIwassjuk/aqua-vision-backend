package com.app.aquavision.dto.cuenta;

import com.app.aquavision.entities.payments.MedioDePago;
import com.app.aquavision.entities.payments.TipoPlan;

import java.time.LocalDate;

public class FacturacionDTO {
    private TipoPlan planActual;
    private double monto;
    private LocalDate fechaProxVencimiento;
    private MedioDePago metodoPago;

    public FacturacionDTO() {
    }

    // Getters & setters
    public TipoPlan getPlanActual() {
        return planActual;
    }

    public FacturacionDTO setPlanActual(TipoPlan planActual) {
        this.planActual = planActual;
        return this;
    }

    public double getMonto() {
        return monto;
    }

    public FacturacionDTO setMonto(double monto) {
        this.monto = monto;
        return this;
    }

    public LocalDate getFechaProxVencimiento() {
        return fechaProxVencimiento;
    }

    public FacturacionDTO setFechaProxVencimiento(LocalDate fechaProxVencimiento) {
        this.fechaProxVencimiento = fechaProxVencimiento;
        return this;
    }

    public MedioDePago getMetodoPago() {
        return metodoPago;
    }

    public FacturacionDTO setMetodoPago(MedioDePago metodoPago) {
        this.metodoPago = metodoPago;
        return this;
    }
}
