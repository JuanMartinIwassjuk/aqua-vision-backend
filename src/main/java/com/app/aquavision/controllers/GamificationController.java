package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.RecompensaHogar;
import com.app.aquavision.services.HogarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@Tag(
        name = "Gamification",
        description = "Operaciones para puntos y recompensas de hogares"
)
@RestController
@RequestMapping("/hogares")
public class GamificationController {

    private static final Logger logger = Logger.getLogger(GamificationController.class.getName());

    @Autowired
    private HogarService service;

    @PostMapping("/{id}/puntos/{puntos}")
    @Operation(
            summary = "Agregar o restar puntos a un hogar",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Puntos modificados correctamente",
                            content = @Content(schema = @Schema(implementation = int.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> addPuntosToHogar(
            @Parameter(description = "ID del hogar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Cantidad de puntos a agregar", example = "5")
            @PathVariable int puntos){

        logger.info("Adding " + puntos + " points to hogar with ID: " + id);

        Hogar hogar = service.addPuntosToHogar(id, puntos);
        int puntosActuales = hogar.getPuntos();

        return ResponseEntity.ok(puntosActuales);
    }

    @PostMapping("/{id}/recompensas/{recompensaId}")
    @Operation(
            summary = "Alta de una recompensa para un hogar",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Recompensa reclamada correctamente",
                            content = @Content(schema = @Schema(implementation = RecompensaHogar.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> canjearRecompensa(
            @Parameter(description = "ID del hogar", example = "1")
            @PathVariable Long id,
            @Parameter(description = "ID de la recompensa a canjear", example = "2")
            @PathVariable Long recompensaId){

        logger.info("Hogar with ID: " + id + " is attempting to redeem reward with ID: " + recompensaId);

        try{
            Hogar hogar = service.reclamarRecompensa(id, recompensaId);
            logger.info("Reward redeemed successfully for hogar ID: " + id);
            return ResponseEntity.status(HttpStatus.CREATED).body(hogar.getRecompensas().getLast());
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }


}
