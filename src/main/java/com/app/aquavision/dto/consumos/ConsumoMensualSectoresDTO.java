package com.app.aquavision.dto.consumos;

import java.util.ArrayList;
import java.util.List;

public class ConsumoMensualSectoresDTO {

    public int mes;

    public float totalMes = 0;

    List<ConsumoTotalSectorDTO> consumosSectorMes = new ArrayList<>();

    public ConsumoMensualSectoresDTO(int mes) {
        this.mes = mes;
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
