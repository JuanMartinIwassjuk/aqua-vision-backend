package com.app.aquavision.services;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.MedicionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteService {

    @Autowired
    private HogarRepository hogarRepository;
    @Autowired
    private MedicionRepository medicionRepository;

    @Transactional(readOnly = true)
    public Hogar findByIdWithSectoresAndMediciones(Long id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Hogar hogar = hogarRepository.findByIdWithSectores(id);
        if (hogar == null) return null;

        hogar.getSectores().forEach(sector -> {
            List<Medicion> mediciones = medicionRepository
                    .findBySectorIdAndFechaBetween(sector.getId(), fechaDesde, fechaHasta);
            sector.setMediciones(mediciones);
        });

        return hogar;
    }


}