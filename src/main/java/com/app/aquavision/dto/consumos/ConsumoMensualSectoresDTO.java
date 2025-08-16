package com.app.aquavision.dto.consumos;

import java.util.ArrayList;
import java.util.List;

public class ConsumoMensualSectoresDTO {

    public int mes;

    public float totalMes = 0;

    public int anio = 0;

    List<ConsumoTotalSectorDTO> consumosSectorMes = new ArrayList<>();

    public ConsumoMensualSectoresDTO(int mes,int anio) {
        this.mes = mes;
        this.anio=anio;
    }

    public int getAnio() {
        return anio;
    }

    public int getMes() {
        return mes;
    }

    public float getTotalMes() {
        return totalMes;
    }

    public void setTotalMes(float totalMes) {
        this.totalMes = totalMes;
    }

    public void addConsumoSector(ConsumoTotalSectorDTO consumo) {
        this.consumosSectorMes.add(consumo);
        totalMes += consumo.getConsumoTotal();
    }

    public List<ConsumoTotalSectorDTO> getConsumosSectorMes() {
        return consumosSectorMes;
    }

}
