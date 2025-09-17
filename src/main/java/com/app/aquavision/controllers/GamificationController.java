package com.app.aquavision.controllers;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.gamification.Recompensa;
import com.app.aquavision.services.HogarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
                            content = @Content(schema = @Schema(implementation = Hogar.class))
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

        return ResponseEntity.ok(hogar);
    }

    @PostMapping("/{id}/recompensas/{recompensaId}")
    @Operation(
            summary = "Alta de una recompensa para un hogar",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Recompensa reclamada correctamente",
                            content = @Content(schema = @Schema(implementation = Hogar.class))
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
            return ResponseEntity.status(HttpStatus.CREATED).body(hogar);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/{id}/racha/aumentar")
    @Operation(
            summary = "Incrementar la racha de un hogar",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Racha incrementada correctamente",
                            content = @Content(schema = @Schema(implementation = Hogar.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> aumentarRachaDiaria(
            @Parameter(description = "ID del hogar", example = "1")
            @PathVariable Long id){

        logger.info("Hogar with ID: " + id + " is attempting to increment their streak");

        try{
            Hogar hogar = service.aumentarRachaDiaria(id);
            logger.info("Reward redeemed successfully for hogar ID: " + id);
            return ResponseEntity.status(HttpStatus.OK).body(hogar);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @PostMapping("/{id}/racha/resetear")
    @Operation(
            summary = "Resetear la racha de un hogar",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Racha incrementada correctamente",
                            content = @Content(schema = @Schema(implementation = Hogar.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Solicitud inválida"
                    )
            }
    )
    public ResponseEntity<?> resetearRachaDiaria(
            @Parameter(description = "ID del hogar", example = "1")
            @PathVariable Long id){

        logger.info("Hogar with ID: " + id + " is attempting to increment their streak");

        try{
            Hogar hogar = service.resetearRachaDiaria(id);
            logger.info("Reward redeemed successfully for hogar ID: " + id);
            return ResponseEntity.status(HttpStatus.OK).body(hogar);
        } catch (IllegalArgumentException e) {
            logger.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @GetMapping("/recompensas")
    @Operation(
            summary = "Listar recompensas reclamables por los hogares",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Racha incrementada correctamente",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = Recompensa.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<Recompensa>> getRecompensas() {

        List<Recompensa> recompensaList = service.getRecompensasDisponibles();

        return ResponseEntity.ok(recompensaList);
    }

}
