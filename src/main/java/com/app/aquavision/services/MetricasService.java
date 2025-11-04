package com.app.aquavision.services;

import com.app.aquavision.dto.metricas.MetricasHogaresDTO;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.TipoHogar;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.MedicionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MetricasService {

    @Autowired
    private HogarRepository hogarRepository;

    @Autowired
    private MedicionRepository medicionRepository;

    @Transactional
    public Long contarHogares() {
        return hogarRepository.count();
    }

    @Transactional(readOnly = true)
    public MetricasHogaresDTO exportarReporteHogares(String localidad, Integer miembros, TipoHogar tipoHogar, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        MetricasHogaresDTO metricas = new MetricasHogaresDTO(localidad, miembros, tipoHogar, fechaDesde, fechaHasta);

        List<Hogar> hogares = hogarRepository.buscarHogaresPorFiltros(localidad, miembros, tipoHogar);

        int totalHogares = hogares.size();
        float consumoTotal = 0f;

        if (totalHogares == 0) {
            metricas.setTotalHogares(0);
            metricas.setConsumoTotal(0f);
            metricas.setConsumoPromedio(0f);
            return metricas;
        }

        // Calcular el consumo total en el rango
        for (Hogar hogar : hogares) {
            consumoTotal += hogar.consumoTotalFechas(fechaDesde, fechaHasta);
        }

        float consumoPromedio = consumoTotal / totalHogares;

        metricas.setTotalHogares(totalHogares);
        metricas.setConsumoTotal(consumoTotal);
        metricas.setConsumoPromedio(consumoPromedio);

        return metricas;
    }




}
