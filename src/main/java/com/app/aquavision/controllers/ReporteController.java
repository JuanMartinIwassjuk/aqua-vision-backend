package com.app.aquavision.controllers;

import com.app.aquavision.dto.ConsumoHogarDTO;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.services.HogarService;
import com.app.aquavision.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, originPatterns = "*")
@RestController
@RequestMapping("/reportes")
public class ReporteController {
    private static final Logger logger = LoggerFactory.getLogger(HogarController.class);

    @Autowired
    private HogarService hogarService;
    @Autowired
    private ReporteService reporteService;

    @GetMapping("/{id}/consumo-actual")
    @Operation(
            summary = "Obtener el consumo actual diario de un hogar",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consumo diario obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumoHogarDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    public ResponseEntity<?> getConsumoActual(
            @Parameter(description = "ID del hogar a consultar", example = "18")
            @PathVariable Long id) {

        logger.info("getConsumoActual - id: {}", id);

        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        LocalDateTime mañana = hoy.plusDays(1).minusSeconds(1);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id,hoy,mañana);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumoHogarDTO consumoDiarioDTO = new ConsumoHogarDTO(hogar);

        return ResponseEntity.ok(consumoDiarioDTO);
    }

    @Operation(
            summary = "Obtener reporte de consumo de un hogar entre dos fechas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte de consumo obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumoHogarDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    @GetMapping("/{id}/consumo")
    public ResponseEntity<?> getReportePorFecha(
            @Parameter(description = "ID del hogar a consultar", example = "18")
            @PathVariable Long id,

            @Parameter(description = "Fecha de inicio en formato yyyy-MM-dd", example = "2025-08-01")
            @RequestParam String fechaInicio,

            @Parameter(description = "Fecha de fin en formato yyyy-MM-dd", example = "2025-08-05")
            @RequestParam String fechaFin) {

        logger.info("getReportePorFecha - id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        LocalDate fechaDesde = LocalDate.parse(fechaInicio);
        LocalDate fechaHasta = LocalDate.parse(fechaFin);

        LocalDateTime desdeDateTime = fechaDesde.atStartOfDay();
        LocalDateTime hastaDateTime = fechaHasta.atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id, desdeDateTime, hastaDateTime);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumoHogarDTO consumoDiarioDTO = new ConsumoHogarDTO(hogar);

        return ResponseEntity.ok(consumoDiarioDTO);
    }

    @GetMapping("/{id}/proyeccion")
    public ResponseEntity<?> getReporteProyeccionPorFecha(@PathVariable Long id,
                                                          @RequestParam String fechaInicio,
                                                          @RequestParam String fechaFin) {

        logger.info("getReportePorFecha - id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        Hogar hogar = hogarService.findById(id);

        //TODO

        return ResponseEntity.ok(0);
    }



}
