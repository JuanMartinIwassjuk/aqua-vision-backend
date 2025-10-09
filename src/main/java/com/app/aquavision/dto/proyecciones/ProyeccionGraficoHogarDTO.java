package com.app.aquavision.dto.proyecciones;

import java.util.ArrayList;
import java.util.List;

public class ProyeccionGraficoHogarDTO {
    private Long hogarId;
    private List<ProyeccionGraficoSectorDTO> proyeccionSectores;

    public ProyeccionGraficoHogarDTO() {
        this.proyeccionSectores = new ArrayList<>();
    }

    public Long getHogarId() {
        return hogarId;
    }

    public void setHogarId(Long hogarId) {
        this.hogarId = hogarId;
    }

    public List<ProyeccionGraficoSectorDTO> getProyeccionSectores() {
        return proyeccionSectores;
    }

    public void setProyeccionSectores(List<ProyeccionGraficoSectorDTO> proyeccionSectores) {
        this.proyeccionSectores = proyeccionSectores;
    }

    public void anadirProyeccionSector(ProyeccionGraficoSectorDTO proyeccionSector) {
        this.proyeccionSectores.add(proyeccionSector);
    }
}
