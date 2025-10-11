package com.app.aquavision.controllers;

import com.app.aquavision.dto.consumos.*;
import com.app.aquavision.dto.proyecciones.ProyeccionGraficoHogarDTO;
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
import java.util.NoSuchElementException;

@Tag(
        name = "Reportes",
        description = "Operaciones para consultar reportes de hogares y sus sectores"
)
@RestController
@RequestMapping("/reportes")
public class ReporteController {
    private static final Logger logger = LoggerFactory.getLogger(ReporteController.class);

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private ProyeccionService proyeccionService;

    @Operation(
            summary = "Obtener el consumo de un hogar por hora de un dia",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consumo por hora de un dia obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumosPorHoraHogarDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    @GetMapping("/{id}/consumo-dia-hora")
    public ResponseEntity<?> getReporteConsumoPorDiaPorHora(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dia en formato yyyy-MM-dd", example = "2025-08-01")
            @RequestParam String dia)
    {
        logger.info("getReporteConsumoPorHoraPorDia - hogar_id: {}, dia: {}", id, dia);

        LocalDateTime diaInicio = LocalDate.parse(dia).atStartOfDay();
        LocalDateTime diaFin = LocalDate.parse(dia).atTime(LocalTime.MAX);

        ConsumosPorHoraHogarDTO consumosPorHoraHogarDTO = this.reporteService.consumosHogarPorHora(id, diaInicio, diaFin);

        return ResponseEntity.ok(consumosPorHoraHogarDTO);
    }

    @Operation(
            summary = "Obtener el consumo de un hogar por sector por hora de un dia",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Consumo por hora de un dia de un sector obtenido correctamente",
                            content = @Content(schema = @Schema(implementation = ConsumosPorHoraSectorDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado"
                    )
            }
    )
    @GetMapping("/{id}/consumo-dia-hora-sectores")
    public ResponseEntity<?> getReporteConsumoPorDiaPorHoraPorSector(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Dia en formato yyyy-MM-dd", example = "2025-08-01")
            @RequestParam String dia)
    {
        logger.info("getReporteConsumoPorHoraPorDiaPorSector - hogar_id: {}, dia: {}", id, dia);

        LocalDateTime diaInicio = LocalDate.parse(dia).atStartOfDay();
        LocalDateTime diaFin = LocalDate.parse(dia).atTime(LocalTime.MAX);

        ConsumosPorHoraSectoresDTO consumosPorHoraSectoresDTO = this.reporteService.consumosSectoresPorHora(id, diaInicio, diaFin);

        return ResponseEntity.ok(consumosPorHoraSectoresDTO);
    }

    @Operation(
            summary = "Obtener reporte de consumo total de un hogar y sus sectores de un dia específico",
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
    @GetMapping("/{id}/consumo-dia")
    public ResponseEntity<?> getReporteConsumoPorDia(
            @Parameter(description = "ID del hogar a consultar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Fecha de inicio en formato yyyy-MM-dd", example = "2025-08-01")
            @RequestParam String dia)
    {
        logger.info("getReporteConsumoPorDia - hogar_id: {}, dia: {}", id, dia);

        LocalDateTime diaInicio = LocalDate.parse(dia).atStartOfDay();
        LocalDateTime diaFin = LocalDate.parse(dia).atTime(LocalTime.MAX);

        ConsumoTotalHogarDTO consumoHogarFecha = reporteService.consumosHogarYSectoresFecha(id, diaInicio, diaFin);

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

        LocalDateTime desdeDateTime = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime hastaDateTime = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);

        ConsumoMensualHogarDTO consumoMensualHogar = reporteService.consumosHogarYSectoresFechaMensual(id, desdeDateTime, hastaDateTime);

        return ResponseEntity.ok(consumoMensualHogar);
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
        LocalDateTime hastaDateTime = fechaHasta.atTime(LocalTime.MAX);

        ConsumoTotalHogarDTO consumoHogarFecha = reporteService.consumosHogarYSectoresFecha(id, desdeDateTime, hastaDateTime);

        return ResponseEntity.ok(consumoHogarFecha);
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
    public ResponseEntity<?> getReporteProyeccionMensual(@PathVariable Long id) {

        logger.info("getReporteProyeccionMensual - hogar_id: {}", id);

        ProyeccionHogarDTO response = proyeccionService.calcularProyeccion(id);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hogarId}/proyeccion-grafico")
    @Operation(
            summary = "Obtener proyección de consumo por hogar",
            description = """
        Genera y devuelve la proyección de consumo de agua para un hogar específico,
        desglosada por sector. Incluye datos históricos, actuales, proyectados y hallazgos relevantes.
        """,
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
                                    schema = @Schema(implementation = ProyeccionGraficoHogarDTO.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Hogar no encontrado",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Hogar no encontrado\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Error interno del servidor\"}"))
                    )
            }
    )
    public ProyeccionGraficoHogarDTO obtenerProyeccionPorHogar(
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
