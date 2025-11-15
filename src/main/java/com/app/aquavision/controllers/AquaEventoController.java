package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.gamification.AquaEvento;
import com.app.aquavision.services.AquaEventoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Eventos",
        description = "Operaciones para crear, consultar y gestionar eventos"
)
@RestController
@RequestMapping("/eventos")
public class AquaEventoController {

    private static final Logger logger = LoggerFactory.getLogger(AquaEventoController.class);

    @Autowired
    private AquaEventoService service;

    @GetMapping
    public List<AquaEvento> list() {
        return service.findAll();
    }

    @GetMapping("/hogar/{hogarId}")
    @Operation(summary = "Listar eventos por hogar ID")
    public List<AquaEvento> listEventosHogar(@PathVariable Long hogarId) {
        return service.findByHogarId(hogarId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener evento por ID")
    public AquaEvento getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @Operation(
            summary = "Crear nuevo evento",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Evento creado correctamente",
                            content = @Content(schema = @Schema(implementation = AquaEvento.class))
                    )
            }
    )
    public ResponseEntity<?> create(@RequestBody AquaEvento evento) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(evento));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar parcialmente un evento")
    public ResponseEntity<?> edit(@PathVariable Long id, @RequestBody AquaEvento updatedEvent) {
        AquaEvento updated = service.editEvent(id, updatedEvent);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping
    @Operation(summary = "Actualizar completamente un evento")
    public ResponseEntity<?> update(@RequestBody AquaEvento updatedEvent) {
        AquaEvento updated = service.updateEvent(updatedEvent);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar evento por ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}