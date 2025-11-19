// src/main/java/com/app/aquavision/controllers/MedicionDataController.java

package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.repositories.MedicionRepository;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(
    name = "Mediciones",
    description = "Consultar ultimas 60 mediciones minuto a minuto de sensor"
)
@RestController
@RequestMapping("/mediciones")
public class MedicionDataController {

    private static final Logger logger = LoggerFactory.getLogger(MedicionDataController.class);

    @Autowired
    private MedicionRepository medicionRepository;

    @GetMapping("/sector/{sectorId}/ultimas")
    public ResponseEntity<List<Medicion>> getUltimasMediciones(
            @PathVariable Long sectorId,
            @RequestParam(defaultValue = "60") int minutos) 
    {
        LocalDateTime fechaHasta = LocalDateTime.now();
        LocalDateTime fechaDesde = fechaHasta.minusMinutes(minutos);
        
        logger.info("Consultando mediciones para Sector ID {} en los últimos {} minutos.", sectorId, minutos);

        // Llamamos al método que ya modificaste en el repositorio
        List<Medicion> mediciones = medicionRepository.findBySectorIdAndTimestampRange(sectorId, fechaDesde, fechaHasta);

        if (mediciones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        return ResponseEntity.ok(mediciones);
    }
}