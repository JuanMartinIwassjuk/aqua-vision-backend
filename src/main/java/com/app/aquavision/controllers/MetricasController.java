package com.app.aquavision.controllers;

import com.app.aquavision.dto.metricas.MetricasHogaresDTO;
import com.app.aquavision.entities.domain.TipoHogar;
import com.app.aquavision.services.MetricasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Tag(
        name = "Metricas",
        description = "Operaciones para consultar metricas de hogares y exportar reportes (administrador)"
)
@RestController
@RequestMapping("/metricas")
public class MetricasController {

    private static final Logger logger = LoggerFactory.getLogger(MetricasController.class);

    @Autowired
    private MetricasService metricasService;

    @GetMapping("/hogares/cantidad")
    @Operation(
            summary = "Contar hogares",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Hogares",
                            content = @Content(schema = @Schema(implementation = Integer.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public Long contarHogares() {

        logger.info("contarHogares - all hogares");

        return metricasService.contarHogares();
    }

    @GetMapping("/hogares/exportar")
    @Operation(
            summary = "Exportar reporte de hogares por filtros",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Reporte exportado exitosamente",
                            content = @Content(schema = @Schema(implementation = MetricasHogaresDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Parámetros de filtro inválidos",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Error interno del servidor",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public ResponseEntity<MetricasHogaresDTO> exportarReporteHogares(
            @Parameter(
                    description = "Localidad del hogar (opcional)",
                    example = "CABA"
            )
            @RequestParam(required = false) String localidad,
            @Parameter(
                    description = "Cantidad minima de miembros del hogar",
                    example = "1"
            )
            @RequestParam(required = true) Integer miembros,
            @Parameter(
                    description = "Tipo de hogar (opcional)",
                    example = "CASA"
            )
            @RequestParam(required = false) TipoHogar tipoHogar,
            @RequestParam(required = true, defaultValue = "2025-10-01") String diaDesde,
            @RequestParam(required = true, defaultValue = "2025-11-04") String diaHasta)
    {

        LocalDateTime fechaDesde = LocalDate.parse(diaDesde).atStartOfDay();
        LocalDateTime fechaHasta = LocalDate.parse(diaHasta).atTime(LocalTime.MAX);

        logger.info("exportarReporteHogares - Exportando reporte de hogares, localidad: {}, miembros: {}, tipoHogar: {}, fechaDesde: {}, fechaHasta: {}",
                localidad, miembros, tipoHogar, fechaDesde, fechaHasta);

        try{
            MetricasHogaresDTO metricas = metricasService.exportarReporteHogares(localidad, miembros, tipoHogar, fechaDesde, fechaHasta);
            return ResponseEntity.ok(metricas);
        } catch (Exception e) {
            logger.info("Error al exportar el reporte de hogares: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }




}
