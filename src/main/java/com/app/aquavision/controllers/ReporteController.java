package com.app.aquavision.controllers;

import com.app.aquavision.dto.ConsumoHogarDTO;
import com.app.aquavision.dto.ConsumosPorHoraHogarDTO;
import com.app.aquavision.dto.ProyeccionHogarDTO;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.services.ProyeccionService;
import com.app.aquavision.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, originPatterns = "*")
@Tag(
        name = "Reportes",
        description = "Operaciones para consultar reportes de hogares y sus sectores"
)
@RestController
@RequestMapping("/reportes")
public class ReporteController {
    private static final Logger logger = LoggerFactory.getLogger(HogarController.class);

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ProyeccionService proyeccionService;

    @GetMapping("/{id}/consumo-actual")
    @Operation(
            summary = "Obtener el consumo actual total de un hogar y sus sectores",
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
    public ResponseEntity<?> getReporteConsumoActual(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id) {

        logger.info("getConsumoActual - id: {}", id);

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id,hoyInicio,hoyFin);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumoHogarDTO consumoDiarioDTO = new ConsumoHogarDTO(hogar, hoyInicio, hoyFin);

        return ResponseEntity.ok(consumoDiarioDTO);
    }

    @Operation(
            summary = "Obtener el consumo actual de un hogar por hora",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consumo diario por hora obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumosPorHoraHogarDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    @GetMapping("/{id}/consumo-actual-hora")
    public ResponseEntity<?> getReporteConsumoPorHora(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id) {

        logger.info("getReporteConsumoPorHora - id: {}", id);

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyActual = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id, hoyInicio, hoyActual);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumosPorHoraHogarDTO consumosPorHoraHogarDTO = new ConsumosPorHoraHogarDTO(hogar, hoyInicio, hoyActual);

        return ResponseEntity.ok(consumosPorHoraHogarDTO);
    }

    @Operation(
            summary = "Obtener reporte de consumo total de un hogar y sus sectores entre dos fechas",
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
    @GetMapping("/{id}/consumo-fecha")
    public ResponseEntity<?> getReporteConsumoPorFecha(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Fecha de inicio en formato yyyy-MM-dd", example = "2025-08-01")
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha de fin en formato yyyy-MM-dd", example = "2025-08-05")
            @RequestParam String fechaFin) {

        logger.info("getReporteConsumoPorFecha - id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        LocalDate fechaDesde = LocalDate.parse(fechaInicio);
        LocalDate fechaHasta = LocalDate.parse(fechaFin);

        LocalDateTime desdeDateTime = fechaDesde.atStartOfDay();
        LocalDateTime hastaDateTime = fechaHasta.atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id, desdeDateTime, hastaDateTime);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumoHogarDTO consumoDiarioDTO = new ConsumoHogarDTO(hogar, desdeDateTime, hastaDateTime);

        return ResponseEntity.ok(consumoDiarioDTO);
    }


    @Operation(
            summary = "Obtener la proyección mensual del hogar",
            description = "Calcula la proyección mensual de consumo de un hogar según el umbral indicado.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID del hogar a consultar",
                            required = true,
                            example = "18"
                    ),
                    @Parameter(
                            name = "umbralMensual",
                            description = "Umbral mensual de consumo",
                            required = true,
                            example = "120.5"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Proyección calculada correctamente",
                            content = @Content(schema = @Schema(implementation = ProyeccionHogarDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    @GetMapping("/{id}/proyeccion")
    public ResponseEntity<?> getReporteProyeccionMensual(@PathVariable Long hogarId,
                                                          @RequestParam double umbralMensual) {

        ProyeccionHogarDTO response = proyeccionService.calcularProyeccion(hogarId,umbralMensual);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}/descargar-reporte-pdf")
    public ResponseEntity<byte[]> descargarReportePDF(@PathVariable Long id) {
            try {
            byte[] pdfBytes = reporteService.generarPdfReporte(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("reporte_consumo.pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

            } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
            } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
    }


}
