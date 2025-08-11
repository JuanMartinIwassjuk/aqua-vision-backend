package com.app.aquavision.controllers;

import com.app.aquavision.dto.ConsumoHogarDTO;
import com.app.aquavision.dto.ConsumoPorHoraDTO;
import com.app.aquavision.dto.ConsumosPorHoraHogarDTO;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.services.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;






import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
            @Parameter(description = "ID del hogar a consultar", example = "18")
            @PathVariable Long id) {

        logger.info("getConsumoActual - id: {}", id);

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id,hoyInicio,hoyFin);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumoHogarDTO consumoDiarioDTO = new ConsumoHogarDTO(hogar);

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
            @Parameter(description = "ID del hogar a consultar", example = "18")
            @PathVariable Long id) {

        logger.info("getReporteConsumoPorHora - id: {}", id);

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyActual = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id, hoyInicio, hoyActual);

        if (hogar == null) {
            return ResponseEntity.notFound().build();
        }

        ConsumosPorHoraHogarDTO consumosPorHoraHogarDTO = new ConsumosPorHoraHogarDTO(hogar);

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
            @Parameter(description = "ID del hogar a consultar", example = "18")
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

        ConsumoHogarDTO consumoDiarioDTO = new ConsumoHogarDTO(hogar);

        return ResponseEntity.ok(consumoDiarioDTO);
    }


    @Operation(
            summary = "Obtener la proyeccion del Hogar (TODO)"
    )
    @GetMapping("/{id}/proyeccion")
    public ResponseEntity<?> getReporteProyeccionPorFecha(@PathVariable Long id,
                                                          @RequestParam String fechaInicio,
                                                          @RequestParam String fechaFin) {

        logger.info("getReporteProyeccionPorFecha - id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        LocalDate fechaDesde = LocalDate.parse(fechaInicio);
        LocalDate fechaHasta = LocalDate.parse(fechaFin);

        LocalDateTime desdeDateTime = fechaDesde.atStartOfDay();
        LocalDateTime hastaDateTime = fechaHasta.atTime(LocalTime.MAX);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(id,desdeDateTime, desdeDateTime);

        //TODO

        return ResponseEntity.ok(0);
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
