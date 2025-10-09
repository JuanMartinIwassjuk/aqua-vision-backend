package com.app.aquavision.dto.proyecciones;

import java.util.List;

public class ProyeccionGraficoSectorDTO {
    private String nombreSector;
    private List<ProyeccionPuntosDTO> puntos;
    private List<String> hallazgosClave;

    public ProyeccionGraficoSectorDTO(List<ProyeccionPuntosDTO> puntos, List<String> hallazgosClave) {
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

    public String getNombreSector() {
        return nombreSector;
    }
    public void setNombreSector(String nombreSector) {
        this.nombreSector = nombreSector;
    }
}
