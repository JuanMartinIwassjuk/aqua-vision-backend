package com.app.aquavision.dto.consumos;

import com.app.aquavision.entities.domain.CategoriaSector;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConsumosPorHoraSectorDTO {

    public Long sectorId;
    public String nombreSector;
    public CategoriaSector categoria;
    public final List<ConsumoPorHoraDTO> consumosPorHora = new ArrayList<>();


}
