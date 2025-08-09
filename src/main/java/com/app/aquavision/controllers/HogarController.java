package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.services.HogarService;
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

@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080"}, originPatterns = "*")
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
    public Hogar list(@PathVariable Long id) {

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
}
