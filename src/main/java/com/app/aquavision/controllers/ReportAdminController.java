package com.app.aquavision.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.app.aquavision.dto.admin.localidad.LocalidadSummaryDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/reportes/admin")
public class ReportAdminController {

    @Autowired
    private com.app.aquavision.services.ReporteService reporteService;

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

    @GetMapping("/eventos/descargar-pdf")
public ResponseEntity<byte[]> descargarReporteEventosPdf(
        @RequestParam String fechaInicio,
        @RequestParam String fechaFin,
        @RequestParam(required = false) List<Integer> tagIds // opcional
) {
    try {
        LocalDate desde = LocalDate.parse(fechaInicio);
        LocalDate hasta = LocalDate.parse(fechaFin);

        byte[] pdfBytes = reporteService.generarPdfReporteEventosAdmin(
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


}