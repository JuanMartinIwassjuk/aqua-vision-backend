package com.app.aquavision.services;

import com.app.aquavision.dto.admin.eventos.AquaEventDTO;
import com.app.aquavision.dto.admin.eventos.EventTagDTO;
import com.app.aquavision.dto.admin.eventos.ResumenEventosDTO;
import com.app.aquavision.dto.admin.eventos.TagRankingDTO;
import com.app.aquavision.entities.domain.gamification.AquaEvento;
import com.app.aquavision.repositories.AquaEventoRepository;
import com.app.aquavision.repositories.TagEventoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportAdminService {

    @Autowired
    private AquaEventoRepository aquaEventoRepository;

    @Autowired
    private TagEventoRepository tagEventoRepository;

    @Autowired
    private TemplateEngine templateEngine;

    private static final double COSTO_POR_LITRO = 0.18;

    // =============================
    //     OBTENER EVENTOS REALES
    // =============================
public List<AquaEventDTO> getEventos(LocalDateTime desde,
                                     LocalDateTime hasta,
                                     List<Integer> tagIds) {

    // DEBUG: log input
    System.out.println("Service.getEventos called with desde=" + desde + " hasta=" + hasta + " tagIds=" + tagIds);

    List<AquaEvento> eventos;

    if (tagIds != null && !tagIds.isEmpty()) {
        eventos = aquaEventoRepository.findEventosBetweenDatesAndTags(desde, hasta, tagIds);
    } else {
        eventos = aquaEventoRepository.findEventosBetweenDates(desde, hasta);
    }

    if (eventos == null) eventos = List.of();

    // Filtrado defensivo: asegurarnos que las fechas de los eventos caen dentro del rango pedido
    List<AquaEvento> eventosFiltrados = eventos.stream()
            .filter(e -> {
                if (e.getFechaInicio() == null) return false;
                LocalDateTime f = e.getFechaInicio();
                // si fechaInicio tiene zona distinta ya fue mapeada a LocalDateTime en la entidad
                return !f.isBefore(desde) && !f.isAfter(hasta);
            })
            .toList();

    // LOG: mostrar resumen de lo recibido vs filtrado
    System.out.println("Service.getEventos: recibidos=" + eventos.size() + ", despuésFiltrado=" + eventosFiltrados.size());
    if (!eventosFiltrados.isEmpty()) {
        eventosFiltrados.stream().limit(10)
                .forEach(ev -> System.out.println(" evt id=" + ev.getId() + " fechaInicio=" + ev.getFechaInicio() + " fechaFin=" + ev.getFechaFin()));
    }

    return eventosFiltrados.stream().map(this::mapToDTO).toList();
}


    private AquaEventDTO mapToDTO(AquaEvento e) {
        AquaEventDTO dto = new AquaEventDTO();
        dto.setId(e.getId());
        dto.setTitulo(e.getTitulo());
        dto.setDescripcion(e.getDescripcion());
        dto.setFechaInicio(e.getFechaInicio());
        dto.setFechaFin(e.getFechaFin());
        dto.setEstado(e.getEstado().name());
 

        // litros y costo
        dto.setLitrosConsumidos((int) Math.round(e.getLitrosConsumidos()));
        dto.setCosto(e.getLitrosConsumidos() * COSTO_POR_LITRO);

        // tags
        if (e.getTags() != null) {
            dto.setTags(
                e.getTags().stream()
                    .map(t -> new EventTagDTO(Math.toIntExact(t.getId()), t.getNombre(), t.getColor()))
                    .toList()
            );
        }

        return dto;
    }

    // =============================
    //  STATS: TOTALES / RANKINGS
    // =============================

    public ResumenEventosDTO calcularResumen(List<AquaEventDTO> eventos) {
        int totalEventos = eventos.size();
        double totalLitros = eventos.stream().mapToDouble(e -> e.getLitrosConsumidos()).sum();
        double totalCosto = eventos.stream().mapToDouble(e -> e.getCosto()).sum();

        return new ResumenEventosDTO(
                totalEventos,
                round(totalLitros),
                round(totalCosto),
                calcularTagsActivos(eventos)
        );
    }

    private int calcularTagsActivos(List<AquaEventDTO> eventos) {
        return eventos.stream()
                .flatMap(e -> e.getTags().stream())
                .map(EventTagDTO::getId)
                .collect(Collectors.toSet())
                .size();
    }

    public List<TagRankingDTO> calcularRankingTags(List<AquaEventDTO> eventos) {

        Map<String, Integer> counts = new HashMap<>();
        Map<String, Double> sumLitros = new HashMap<>();
        Map<String, Integer> tagIdMap = new HashMap<>();

        for (AquaEventDTO e : eventos) {
            int litros = e.getLitrosConsumidos();

            for (EventTagDTO t : e.getTags()) {
                String key = t.getNombre();

                counts.put(key, counts.getOrDefault(key, 0) + 1);
                sumLitros.put(key, sumLitros.getOrDefault(key, 0.0) + litros);
                tagIdMap.put(key, t.getId());
            }
        }

        return counts.entrySet().stream()
                .map(entry -> {
                    String nombre = entry.getKey();
                    int count = entry.getValue();
                    double avgLitros = round(sumLitros.get(nombre) / count);
                    Integer id = tagIdMap.get(nombre);
                    return new TagRankingDTO(id, nombre, count, avgLitros);
                })
                .sorted(Comparator.comparing(TagRankingDTO::getCount).reversed())
                .toList();
    }

    // =============================
    //  EVENTOS POR DÍA
    // =============================
    public Map<String, Long> eventosPorDia(List<AquaEventDTO> eventos) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return eventos.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getFechaInicio().toLocalDate().format(fmt),
                        Collectors.counting()
                ));
    }

    // =============================
    //  EVENTOS POR TAG
    // =============================
    public Map<String, Long> eventosPorTag(List<AquaEventDTO> eventos) {
        return eventos.stream()
                .flatMap(e -> e.getTags().stream())
                .collect(Collectors.groupingBy(
                        EventTagDTO::getNombre,
                        Collectors.counting()
                ));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // =============================
    //    PDF: Reporte eventos
    // =============================
    public byte[] generarPdfReporteEventos(LocalDateTime desde,
                                           LocalDateTime hasta,
                                           List<Integer> tagIds) {

        List<AquaEventDTO> eventos = getEventos(desde, hasta, tagIds);

        ResumenEventosDTO resumen = calcularResumen(eventos);
        List<TagRankingDTO> ranking = calcularRankingTags(eventos);

        // Datos para Thymeleaf
        Context context = new Context();
        context.setVariable("fechaDesde", desde.toLocalDate().toString());
        context.setVariable("fechaHasta", hasta.toLocalDate().toString());
        context.setVariable("fechaGeneracion", LocalDateTime.now());
        context.setVariable("resumen", resumen);
        context.setVariable("eventos", eventos);
        context.setVariable("tagRanking", ranking);
        context.setVariable("eventosPorDia", eventosPorDia(eventos));
        context.setVariable("eventosPorTag", eventosPorTag(eventos));

        // Render HTML
        String html = templateEngine.process("admin-eventos-report", context);

        // Export PDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF eventos admin", e);
        }
    }
}