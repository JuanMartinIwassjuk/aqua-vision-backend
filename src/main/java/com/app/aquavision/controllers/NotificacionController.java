package com.app.aquavision.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.services.NotificacionService;

import java.util.List;


@RestController
@RequestMapping("/notificaciones/{hogarId}")
@Tag(name = "Notificaciones", description = "Endpoints para gestionar notificaciones por hogar.")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;


    @Operation(
        summary = "Obtener todas las notificaciones de un hogar",
        description = "Devuelve todas las notificaciones (leídas y no leídas) asociadas al hogar indicado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Notificacion.class))),
        @ApiResponse(responseCode = "404", description = "Hogar no encontrado")
    })
    @GetMapping
    public ResponseEntity<List<Notificacion>> getNotifications(
        @Parameter(description = "ID del hogar", example = "1") @PathVariable Long hogarId
    ) {
        List<Notificacion> notifs = notificacionService.getNotifications(hogarId);
        return ResponseEntity.ok(notifs);
    }

    @Operation(
        summary = "Obtener notificaciones no leídas",
        description = "Devuelve únicamente las notificaciones pendientes de lectura del hogar especificado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de notificaciones no leídas obtenida correctamente",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Notificacion.class)))
    })
    @GetMapping("/no-leidas")
    public ResponseEntity<List<Notificacion>> getUnreadNotifications(
        @Parameter(description = "ID del hogar", example = "1") @PathVariable Long hogarId
    ) {
        return ResponseEntity.ok(notificacionService.getUnreadNotifications(hogarId));
    }


    @Operation(
        summary = "Marcar una notificación como leída",
        description = "Actualiza el estado de una notificación específica a 'leído'."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notificación marcada como leída correctamente"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @PutMapping("/{id}/leer")
    public ResponseEntity<?> markAsRead(
        @Parameter(description = "ID de la notificación", example = "5") @PathVariable Long id
    ) {
        Notificacion actualizada = notificacionService.markAsRead(id);
        return actualizada != null ? ResponseEntity.ok(actualizada) : ResponseEntity.notFound().build();
    }

    
    @Operation(
        summary = "Marcar todas las notificaciones como leídas",
        description = "Marca como leídas todas las notificaciones de un hogar específico."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todas las notificaciones marcadas como leídas"),
        @ApiResponse(responseCode = "404", description = "No se encontraron notificaciones para el hogar")
    })
    @PutMapping("/leer-todas")
    public ResponseEntity<Void> markAllAsRead(
        @Parameter(description = "ID del hogar", example = "1") @PathVariable Long hogarId
    ) {
        boolean updated = notificacionService.markAllAsRead(hogarId);
        return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }


    @Operation(
        summary = "Eliminar una notificación por ID",
        description = "Elimina una notificación específica del sistema."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notificación eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Notificación no encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
        @Parameter(description = "ID de la notificación a eliminar", example = "3") @PathVariable Long id
    ) {
        boolean deleted = notificacionService.deleteNotification(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }


    @Operation(
        summary = "Eliminar todas las notificaciones de un hogar",
        description = "Borra todas las notificaciones (leídas y no leídas) asociadas al hogar indicado."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Todas las notificaciones eliminadas correctamente")
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(
        @Parameter(description = "ID del hogar", example = "1") @PathVariable Long hogarId
    ) {
        notificacionService.deleteAllNotifications(hogarId);
        return ResponseEntity.noContent().build();
    }
}