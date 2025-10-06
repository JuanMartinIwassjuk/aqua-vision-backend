package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.services.MetricasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Metricas",
        description = "Operaciones para consultar metricas de hogares (administrador)"
)
@RestController
@RequestMapping("/metricas")
public class MetricasController {

    private static final Logger logger = LoggerFactory.getLogger(MetricasController.class);

    @Autowired
    private MetricasService metricasService;

    @GetMapping("/hogares/count")
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

    //@GetMapping("/hogares/consumos-por-localidad")




}
