package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.services.HogarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Tag(
        name = "Hogares",
        description = "Operaciones para crear y consultar hogares"
)
@RestController
@RequestMapping("/hogares")
public class HogarController {

    private static final Logger logger = LoggerFactory.getLogger(HogarController.class);

    @Autowired
    private HogarService service;

    @GetMapping
    public List<Hogar> list() {

        logger.info("list - all hogares");

        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener hogar por ID")
    public Hogar list(
            @Parameter(
                    description = "ID del hogar a buscar",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        logger.info("list hogar - id: {}", id);
        return service.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Alta de un nuevo hogar y sus sectores",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Hogar creado correctamente",
                            content = @Content(schema = @Schema(implementation = Hogar.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> create(@RequestBody @Valid Hogar hogar) {

        logger.info("create - hogar: {}", hogar);

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(hogar));
    }

    @GetMapping("/{id}/sectores")
    public List<Sector> getSectoresByHogar(@PathVariable Long id) {
        Hogar hogar = service.findById(id);
        return hogar != null ? hogar.getSectores() : Collections.emptyList();
    }

    @PostMapping("/{id}/sectores/{id_sector}/umbral/{nuevoUmbral}")
    @Operation(
            summary = "Modificar umbral de un sector",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Umbral actualizado correctamente",
                            content = @Content(schema = @Schema(implementation = Hogar.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> updateUmbralSector(@PathVariable Long id, @PathVariable Long id_sector, @PathVariable Float nuevoUmbral) {

        logger.info("update - umbral {}, sector: {}, hogar: {}", nuevoUmbral, id_sector, id);

        Hogar hogar = service.updateUmbralSector(id, id_sector, nuevoUmbral);

        if (hogar == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hogar o sector no encontrado");
        }

        return ResponseEntity.status(HttpStatus.OK).body(service.updateUmbralSector(id, id_sector, nuevoUmbral));
    }

    @PostMapping("/{id}/notificaciones/{id_notificacion}/leer")
    @Operation(
            summary = "Marcar una notificacion como leida",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Notificacion actualizada correctamente",
                            content = @Content(schema = @Schema(implementation = Hogar.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> visualizarNotificacion(@PathVariable Long id, @PathVariable Long id_notificacion) {

        logger.info("update - hogar {}, notificacion {}", id, id_notificacion);

        Hogar hogar = service.visualizarNotificacion(id, id_notificacion);

        if (hogar == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Hogar o notificacion no encontrada");
        }

        return ResponseEntity.status(HttpStatus.OK).body(service.visualizarNotificacion(id, id_notificacion));
    }


}
