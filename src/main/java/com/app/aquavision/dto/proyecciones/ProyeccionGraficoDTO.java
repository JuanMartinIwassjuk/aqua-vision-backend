package com.app.aquavision.dto.proyecciones;

import java.util.List;

public class ProyeccionGraficoDTO {

    private List<ProyeccionPuntosDTO> puntos;
    private List<String> hallazgosClave;

    public ProyeccionGraficoDTO(List<ProyeccionPuntosDTO> puntos, List<String> hallazgosClave) {
        this.puntos = puntos;
        this.hallazgosClave = hallazgosClave;
    }

    // Getters
    public List<ProyeccionPuntosDTO> getPuntos() {
        return puntos;
    }

    public List<String> getHallazgosClave() {
        return hallazgosClave;
    }
}
