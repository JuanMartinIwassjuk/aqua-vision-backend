package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Medidor;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.services.MedidorService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@Tag(
        name = "Medidores",
        description = "Operaciones para crear y consultar estado de medidores"
)
@RestController
@RequestMapping("/medidores")
public class MedidorController {

    private static final Logger logger = LoggerFactory.getLogger(MedidorController.class);

    @Autowired
    private MedidorService service;

    @GetMapping
    public List<Medidor> list() {

        logger.info("list - all medidores");

        return service.findAll();
    }

    @GetMapping("/{id}")
    public Medidor list(@PathVariable Long id) {

        logger.info("list medidor - id: {}", id);

        return service.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Alta de un nuevo medidor",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Medidor creado correctamente",
                            content = @Content(schema = @Schema(implementation = Medidor.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> create(
            @RequestParam("sectorId") Long sectorId,
            @RequestParam("numeroSerie") int numeroSerie) {

        logger.info("create - medidor: {}, sectorId: {}", numeroSerie, sectorId);

        Sector sector = service.findSectorById(sectorId);
        if (sector == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Sector not found with id: " + sectorId);
        }

        Medidor medidor = new Medidor(numeroSerie, sector);

        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(medidor));
    }
}
