package com.app.aquavision.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.app.aquavision.dto.admin.eventos.AquaEventDTO;
import com.app.aquavision.dto.admin.eventos.ResumenEventosDTO;
import com.app.aquavision.dto.admin.eventos.TagRankingDTO;
import com.app.aquavision.dto.admin.gamification.HogarRankingDTO;
import com.app.aquavision.dto.admin.gamification.MedallasHogarDTO;
import com.app.aquavision.dto.admin.gamification.PuntosDiaDTO;
import com.app.aquavision.dto.admin.gamification.ResumenGamificacionDTO;
import com.app.aquavision.dto.admin.localidad.LocalidadSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reportes/admin")
public class ReportAdminController {

    @Autowired
    private com.app.aquavision.services.ReporteService reporteService;

    @Autowired
    private com.app.aquavision.services.ReportAdminService reporteAdminService;

    @GetMapping("/consumo/descargar-pdf")
    public ResponseEntity<byte[]> descargarReporteConsumoPdf(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            LocalDate desde = LocalDate.parse(fechaInicio);
            LocalDate hasta = LocalDate.parse(fechaFin);

            byte[] pdfBytes = reporteService.generarPdfReporteConsumoAdmin(
                    desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("reporte_consumo_admin_" + fechaInicio + "_a_" + fechaFin + ".pdf")
                    .build());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
// empieza eventos
    @GetMapping("/eventos")
    public ResponseEntity<List<AquaEventDTO>> obtenerEventos(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(required = false) List<Integer> tagIds
    ) {
        LocalDateTime desde = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime hasta = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);

        List<AquaEventDTO> eventos = reporteAdminService.getEventos(desde, hasta, tagIds);
        return ResponseEntity.ok(eventos);
    }


    @GetMapping("/eventos/resumen")
    public ResponseEntity<ResumenEventosDTO> obtenerResumenEventos(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(required = false) List<Integer> tagIds
    ) {
        LocalDateTime desde = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime hasta = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);

        List<AquaEventDTO> eventos = reporteAdminService.getEventos(desde, hasta, tagIds);
        ResumenEventosDTO resumen = reporteAdminService.calcularResumen(eventos);

        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/eventos/ranking")
    public ResponseEntity<List<TagRankingDTO>> obtenerRankingTags(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(required = false) List<Integer> tagIds
    ) {
        LocalDateTime desde = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime hasta = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);

        List<AquaEventDTO> eventos = reporteAdminService.getEventos(desde, hasta, tagIds);
        List<TagRankingDTO> ranking = reporteAdminService.calcularRankingTags(eventos);

        return ResponseEntity.ok(ranking);
    }

    @GetMapping("/eventos/por-dia")
    public ResponseEntity<List<Map<String, Object>>> obtenerEventosPorDia(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin,
            @RequestParam(required = false) List<Integer> tagIds
    ) {
        LocalDateTime desde = LocalDate.parse(fechaInicio).atStartOfDay();
        LocalDateTime hasta = LocalDate.parse(fechaFin).atTime(LocalTime.MAX);

        List<AquaEventDTO> eventos = reporteAdminService.getEventos(desde, hasta, tagIds);
        Map<String, Long> porDia = reporteAdminService.eventosPorDia(eventos);

        List<Map<String, Object>> respuesta = porDia.entrySet().stream()
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fecha", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(respuesta);
    }



    @GetMapping("/eventos/descargar-pdf")
public ResponseEntity<byte[]> descargarReporteEventosPdf(
        @RequestParam String fechaInicio,
        @RequestParam String fechaFin,
        @RequestParam(required = false) List<Integer> tagIds // opcional
) {
    try {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);

        byte[] pdfBytes = reporteAdminService.generarPdfReporteEventos(
                desde.atStartOfDay(), hasta.atTime(LocalTime.MAX), tagIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("reporte_eventos_admin_" + fechaInicio + "_a_" + fechaFin + ".pdf")
                .build());

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    //empieza localidad

    @GetMapping("/localidad")
    public ResponseEntity<List<LocalidadSummaryDTO>> consumoPorLocalidad(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);
        List<LocalidadSummaryDTO> resumen = reporteService.getConsumoPorLocalidad(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));
        return ResponseEntity.ok(resumen);
    }

    @GetMapping("/localidad/descargar-pdf")
    public ResponseEntity<byte[]> descargarReporteLocalidadPdf(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        try {
            LocalDate desde = LocalDate.parse(fechaInicio);
            LocalDate hasta = LocalDate.parse(fechaFin);
            byte[] pdfBytes = reporteService.generarPdfReporteLocalidad(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("reporte_localidad_" + fechaInicio + "_a_" + fechaFin + ".pdf")
                    .build());

            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            // loguear
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //empieza gamification

      @GetMapping("/gamificacion/puntos-periodo")
    public ResponseEntity<List<PuntosDiaDTO>> puntosPeriodo(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);
        return ResponseEntity.ok(reporteService.getPuntosPorPeriodo(desde, hasta));
    }

    @GetMapping("/gamificacion/resumen")
    public ResponseEntity<ResumenGamificacionDTO> resumenGamificacion(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);
        return ResponseEntity.ok(reporteService.getResumenGamificacion(desde, hasta));
    }

    @GetMapping("/gamificacion/ranking-puntos")
    public ResponseEntity<List<HogarRankingDTO>> rankingPuntos(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);
        return ResponseEntity.ok(reporteService.getRankingPuntos(desde, hasta));
    }

    @GetMapping("/gamificacion/ranking-rachas")
    public ResponseEntity<List<HogarRankingDTO>> rankingRachas(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);
        return ResponseEntity.ok(reporteService.getRankingRachas(desde, hasta));
    }

    @GetMapping("/gamificacion/hogares")
    public ResponseEntity<List<HogarRankingDTO>> hogaresResumen() {
        return ResponseEntity.ok(reporteService.getHogaresSummary());
    }

    @GetMapping("/gamificacion/hogares/{id}/medallas")
    public ResponseEntity<MedallasHogarDTO> medallasPorHogar(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.getMedallasPorHogar(id));
    }

    @GetMapping("/gamificacion/descargar-pdf")
    public ResponseEntity<byte[]> descargarReporteGamificacionPdf(@RequestParam String fechaInicio, @RequestParam String fechaFin) {
        try {
            LocalDate desde = LocalDate.parse(fechaInicio);
            LocalDate hasta = LocalDate.parse(fechaFin);
            byte[] pdf = reporteService.generarPdfReporteGamificacion(desde, hasta);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.builder("attachment")
                    .filename("reporte_gamificacion_" + fechaInicio + "_a_" + fechaFin + ".pdf")
                    .build());
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}