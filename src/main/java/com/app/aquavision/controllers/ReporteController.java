package com.app.aquavision.controllers;

import com.app.aquavision.dto.consumos.ConsumoMensualHogarDTO;
import com.app.aquavision.dto.consumos.ConsumoTotalHogarDTO;
import com.app.aquavision.dto.consumos.ConsumosPorHoraHogarDTO;
import com.app.aquavision.dto.proyecciones.ProyeccionGraficoDTO;
import com.app.aquavision.dto.proyecciones.ProyeccionHogarDTO;
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
import java.util.Map;
import java.util.NoSuchElementException;

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
                            content = @Content(schema = @Schema(implementation = ConsumoTotalHogarDTO.class))
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

        logger.info("getReporteConsumoActual - hogar_id: {}", id);

        ConsumoTotalHogarDTO consumoDiarioDTO = this.reporteService.consumosHogarYSectoresDia(id);

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

        logger.info("getReporteConsumoPorHora - hogar_id: {}", id);

        ConsumosPorHoraHogarDTO consumosPorHoraHogarDTO = this.reporteService.consumosHogarPorHora(id);

        return ResponseEntity.ok(consumosPorHoraHogarDTO);
    }

    @Operation(
            summary = "Obtener reporte de consumo total de un hogar y sus sectores entre dos fechas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte de consumo obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumoTotalHogarDTO.class))
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

        logger.info("getReporteConsumoPorFecha - hogar_id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        LocalDate fechaDesde = LocalDate.parse(fechaInicio);
        LocalDate fechaHasta = LocalDate.parse(fechaFin);

        LocalDateTime desdeDateTime = fechaDesde.atStartOfDay();
        LocalDateTime hastaDateTime = fechaHasta.atTime(LocalTime.MIN);

        ConsumoTotalHogarDTO consumoHogarFecha = reporteService.consumosHogarYSectoresFecha(id, desdeDateTime, hastaDateTime);

        return ResponseEntity.ok(consumoHogarFecha);
    }

    @Operation(
            summary = "Obtener reporte de consumo mensual de un hogar y sus sectores entre dos fechas",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte mensual de consumo obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumoMensualHogarDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    @GetMapping("/{id}/consumo-fecha-mensual")
    public ResponseEntity<?> getReporteConsumoPorFechaMensual(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Fecha de inicio en formato yyyy-MM-dd", example = "2025-05-01")
            @RequestParam String fechaInicio,
            @Parameter(description = "Fecha de fin en formato yyyy-MM-dd", example = "2025-08-05")
            @RequestParam String fechaFin) {

        logger.info("getReporteConsumoPorFechaMensual - hogar_id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        LocalDate fechaDesde = LocalDate.parse(fechaInicio);
        LocalDate fechaHasta = LocalDate.parse(fechaFin).plusDays(1);

        LocalDateTime desdeDateTime = fechaDesde.atStartOfDay();
        LocalDateTime hastaDateTime = fechaHasta.atTime(LocalTime.MAX);

        ConsumoMensualHogarDTO consumoMensualHogar = reporteService.consumosHogarYSectoresFechaMensual(id, desdeDateTime, hastaDateTime);

        return ResponseEntity.ok(consumoMensualHogar);
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
    public ResponseEntity<?> getReporteProyeccionMensual(@PathVariable Long id,
                                                          @RequestParam double umbralMensual) {

        logger.info("getReporteProyeccionMensual - hogar_id: {}, umbralMensual: {}", id, umbralMensual);

        ProyeccionHogarDTO response = proyeccionService.calcularProyeccion(id,umbralMensual);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hogarId}/proyeccion-grafico")
    @Operation(
            summary = "Obtener proyección de consumo por hogar",
            description = "Genera y devuelve la proyección de consumo de energía para un hogar específico, desglosada por sector. Incluye datos históricos, actuales, proyectados y hallazgos relevantes.",
            parameters = {
                    @Parameter(
                            name = "hogarId",
                            description = "ID del hogar para el que se generará la proyección",
                            required = true,
                            example = "2"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Proyección de consumo generada exitosamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class) // Puedes usar una clase de respuesta más específica si la tienes
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor"
                    )
            }
    )
    public Map<String, ProyeccionGraficoDTO> obtenerProyeccionPorHogar(
            @Parameter(description = "ID del hogar para el que se generará la proyección")
            @PathVariable Long hogarId) {
        return proyeccionService.generarProyeccionPorHogar(hogarId);
    }

    @GetMapping("/{id}/descargar-reporte-pdf")
    public ResponseEntity<byte[]> descargarReportePDF(
            @PathVariable Long id,
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        logger.info("descargarReportePDF - hogar_id: {}, fechaInicio: {}, fechaFin: {}", id, fechaInicio, fechaFin);

        try {
            LocalDate desde = LocalDate.parse(fechaInicio);
            LocalDate hasta = LocalDate.parse(fechaFin);

            byte[] pdfBytes = reporteService.generarPdfReporte(id, desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("reporte_consumo.pdf")
                    .build());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

            }
        catch (NoSuchElementException e) {
                return ResponseEntity.notFound().build();
            }
        catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
    }

}
