package com.app.aquavision.services;

import com.app.aquavision.dto.admin.consumo.HogarConsumoDTO;
import com.app.aquavision.dto.admin.consumo.ReporteConsumoAdminDTO;
import com.app.aquavision.dto.admin.consumo.ResumenConsumoGlobalDTO;
import com.app.aquavision.dto.admin.eventos.AquaEventDTO;
import com.app.aquavision.dto.admin.eventos.EventTagDTO;
import com.app.aquavision.dto.admin.eventos.ReporteEventosAdminDTO;
import com.app.aquavision.dto.admin.eventos.ResumenEventosDTO;
import com.app.aquavision.dto.admin.eventos.TagRankingDTO;
import com.app.aquavision.dto.admin.localidad.LocalidadSummaryDTO;
import com.app.aquavision.dto.admin.localidad.ReporteLocalidadDTO;
import com.app.aquavision.dto.consumos.*;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.MedicionRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class ReporteService {

    @Autowired
    private HogarRepository hogarRepository;
    @Autowired
    private MedicionRepository medicionRepository;

    @Transactional(readOnly = true)
    public Hogar findByIdWithSectoresAndMediciones(Long id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Optional<Hogar> opcional = hogarRepository.findByIdWithSectores(id);
        Hogar hogar = opcional.orElseThrow(() -> new NoSuchElementException("Hogar no encontrado con id: " + id));
        hogar.getSectores().forEach(sector -> {
            List<Medicion> mediciones = medicionRepository
                    .findBySectorIdAndFechaBetween(sector.getId(), fechaDesde, fechaHasta);
            sector.setMediciones(mediciones);
        });

        return hogar;
    }

    public void setConsumosHogarDTO(Hogar hogar, ConsumoTotalHogarDTO consumoTotalHogarDTO) {
        for (Sector sector: hogar.getSectores()) {
            ConsumoTotalSectorDTO consumoSector = new ConsumoTotalSectorDTO(sector);
            consumoTotalHogarDTO.addConsumoSector(consumoSector);
            consumoTotalHogarDTO.sumarConsumoTotal(consumoSector.getConsumoTotal());
        }

        if (!consumoTotalHogarDTO.getConsumosPorSector().isEmpty()) {
            consumoTotalHogarDTO.setConsumoPromedio(
                    consumoTotalHogarDTO.getConsumosPorSector().stream()
                            .mapToInt(ConsumoTotalSectorDTO::getConsumoPromedio)
                            .sum()
            );
            consumoTotalHogarDTO.setConsumoPico(
                    consumoTotalHogarDTO.getConsumosPorSector().stream()
                            .mapToInt(ConsumoTotalSectorDTO::getConsumoPico)
                            .max()
                            .orElse(0)
            );
        }
    }

    public ConsumoTotalHogarDTO consumosHogarYSectoresDia(Long hogar_id){

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyFin = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogar_id, hoyInicio, hoyFin);

        ConsumoTotalHogarDTO consumoTotalHogarDTO = new ConsumoTotalHogarDTO(hogar, hoyInicio, hoyFin);

        this.setConsumosHogarDTO(hogar, consumoTotalHogarDTO);

        return consumoTotalHogarDTO;
    }

    public ConsumoTotalHogarDTO consumosHogarYSectoresFecha(Long hogar_id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {

        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogar_id, fechaDesde, fechaHasta);

        ConsumoTotalHogarDTO consumoTotalHogarDTO = new ConsumoTotalHogarDTO(hogar, fechaDesde, fechaHasta);

        this.setConsumosHogarDTO(hogar, consumoTotalHogarDTO);

        return consumoTotalHogarDTO;
    }

    public ConsumosPorHoraHogarDTO consumosHogarPorHora(Long hogar_id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {

        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogar_id, fechaDesde, fechaHasta);

        ConsumosPorHoraHogarDTO consumosPorHoraHogarDTO = new ConsumosPorHoraHogarDTO(hogar_id, fechaDesde, fechaHasta);

        float consumoTotalDia = 0;
        for (int i = 0; i < 24; i++) {
            int consumo = hogar.consumoTotalHora(i);
            consumosPorHoraHogarDTO.addConsumoPorHora(new ConsumoPorHoraDTO(i, consumo));
            consumoTotalDia += consumo;
        }
        consumosPorHoraHogarDTO.setConsumoTotal(consumoTotalDia);

        return consumosPorHoraHogarDTO;
    }

    public ConsumosPorHoraSectoresDTO consumosSectoresPorHora(Long hogar_id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {

        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogar_id, fechaDesde, fechaHasta);

        ConsumosPorHoraSectoresDTO consumosPorHoraSectoresDTO = new ConsumosPorHoraSectoresDTO(hogar_id, fechaDesde, fechaHasta);

        for (Sector sector : hogar.getSectores()) {
            ConsumosPorHoraSectorDTO consumoPorHoraSectorDTO = new ConsumosPorHoraSectorDTO();
            consumoPorHoraSectorDTO.sectorId = sector.getId();
            consumoPorHoraSectorDTO.nombreSector = sector.getNombre();
            consumoPorHoraSectorDTO.categoria = sector.getCategoria();

            float consumoTotalSector = 0;
            for (int i = 0; i < 24; i++) {
                int consumo = sector.totalConsumo(i);
                consumoPorHoraSectorDTO.consumosPorHora.add(new ConsumoPorHoraDTO(i, consumo));
                consumoTotalSector += consumo;
            }
            consumosPorHoraSectoresDTO.setConsumoTotal(consumosPorHoraSectoresDTO.getConsumoTotal() + consumoTotalSector);
            consumosPorHoraSectoresDTO.addConsumoPorHora(consumoPorHoraSectorDTO);
        }

        return consumosPorHoraSectoresDTO;
    }

    public ConsumoMensualHogarDTO consumosHogarYSectoresFechaMensual(Long hogarId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {

        ConsumoMensualHogarDTO consumoMensualHogarDTO = new ConsumoMensualHogarDTO(hogarId, fechaDesde, fechaHasta);

        while (fechaDesde.isBefore(fechaHasta)) {

            Hogar hogar = this.findByIdWithSectoresAndMediciones(hogarId, fechaDesde, fechaDesde.plusMonths(1).minusDays(1));

            int mes = fechaDesde.getMonthValue();
            int anio = fechaDesde.getYear();

            ConsumoMensualSectoresDTO consumoMensualSectoresDTO = new ConsumoMensualSectoresDTO(mes,anio);
            for (Sector sector : hogar.getSectores()) {
                ConsumoTotalSectorDTO consumoTotalSectorDTO = new ConsumoTotalSectorDTO(sector);
                consumoMensualSectoresDTO.addConsumoSector(consumoTotalSectorDTO);
            }
            consumoMensualHogarDTO.addConsumoMensualSector(consumoMensualSectoresDTO);

            fechaDesde = fechaDesde.plusMonths(1);
        }

        return consumoMensualHogarDTO;
    }

@Autowired
private TemplateEngine templateEngine;

public byte[] generarPdfReporte(Long hogarId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {

    if (hogarId == null) {
        throw new NoSuchElementException("Hogar id es null");
    }

    Hogar hogar = this.findByIdWithSectoresAndMediciones(hogarId, fechaDesde, fechaHasta);
    if (hogar == null) {
        throw new NoSuchElementException("Hogar no encontrado con id: " + hogarId);
    }

    double costoPorLitro = 3.0;
    List<ReporteDiarioSectorDTO> sectores = new ArrayList<>();

    int consumoTotal = 0;
    for (Sector s : hogar.getSectores()) {
        int consumo = s.totalConsumo();
        float promedio = s.promedioConsumo();
        float pico = s.picoConsumo();
        double costo = consumo * costoPorLitro;

        sectores.add(new ReporteDiarioSectorDTO(
                s.getNombre(),
                consumo,
                promedio,
                pico,
                costo
        ));

        consumoTotal += consumo;
    }

    double costoTotal = consumoTotal * costoPorLitro;

    ReporteDiarioHogarDTO dto = new ReporteDiarioHogarDTO(
            hogar.getId(),
            hogar.getLocalidad(),
            hogar.getMiembros(),
            fechaDesde,
            fechaHasta,
            consumoTotal,
            costoTotal,
            sectores
    );

    // --- Contexto Thymeleaf ---
    DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    Context context = new Context();
    context.setVariable("localidad", dto.getLocalidad());
    context.setVariable("miembros", dto.getMiembros());
    context.setVariable("consumoTotal", dto.getConsumoTotal());
    context.setVariable("costoTotal", String.format("%.2f", dto.getCostoTotal()));
    context.setVariable("fechaDesde", dto.getFechaDesde().format(fechaFormatter));
    context.setVariable("fechaHasta", dto.getFechaHasta().format(fechaFormatter));
    context.setVariable("fechaGeneracion", dto.getFechaGeneracion().format(fechaHoraFormatter));

    // Detalle por sector
    context.setVariable("consumosPorSector", dto.getConsumosPorSector());

    // --- Renderizar HTML ---
    String htmlContent = templateEngine.process("historical-report", context);

    // --- Generar PDF ---
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.withHtmlContent(htmlContent, "");
        builder.toStream(baos);
        builder.run();
        return baos.toByteArray();
    } catch (Exception e) {
        throw new RuntimeException("Error generando PDF", e);
    }
}

  public byte[] generarPdfReporteConsumoAdmin(LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        // 1) Obtén datos globales: resumen y lista de hogares con consumo.
        // Aquí debes reemplazar la lógica mock por llamadas reales a repositorios.
        ResumenConsumoGlobalDTO resumen = calcularResumenMock(fechaDesde, fechaHasta);
        List<HogarConsumoDTO> hogares = generarHogaresMock(fechaDesde, fechaHasta);

        // 2) Crear DTO para Thymeleaf
        ReporteConsumoAdminDTO dto = new ReporteConsumoAdminDTO(
                LocalDateTime.now(),
                fechaDesde.toLocalDate().toString(),
                fechaHasta.toLocalDate().toString(),
                resumen,
                hogares
        );

        // 3) Context Thymeleaf
        DateTimeFormatter fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Context context = new Context();
        context.setVariable("fechaDesde", dto.getFechaDesde());
        context.setVariable("fechaHasta", dto.getFechaHasta());
        context.setVariable("fechaGeneracion", dto.getFechaGeneracion().format(fechaHoraFormatter));
        context.setVariable("resumen", dto.getResumen());
        context.setVariable("hogares", dto.getHogares());

        String html = templateEngine.process("admin-consumo-report", context);

        // 4) Render to PDF
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF admin", e);
        }
    }

    // --- XLSX ---
    

    // --- MOCK helpers (reemplazar por lógica real) ---
    private ResumenConsumoGlobalDTO calcularResumenMock(LocalDateTime desde, LocalDateTime hasta) {
        // mock simple
        double total = 12345.0;
        double media = 345.3;
        double pico = 820.0;
        double costo = total * 0.18;
        return new ResumenConsumoGlobalDTO(total, media, pico, Math.round(costo*100.0)/100.0);
    }

    private List<HogarConsumoDTO> generarHogaresMock(LocalDateTime desde, LocalDateTime hasta) {
        List<HogarConsumoDTO> list = new ArrayList<>();
        list.add(new HogarConsumoDTO(1L, "Hogar A", "Palermo", 3, 1234.0, Math.round(1234.0*0.18*100.0)/100.0));
        list.add(new HogarConsumoDTO(2L, "Hogar B", "Belgrano", 4, 2345.0, Math.round(2345.0*0.18*100.0)/100.0));
        list.add(new HogarConsumoDTO(3L, "Hogar C", "Caballito", 2, 345.0, Math.round(345.0*0.18*100.0)/100.0));
        return list;
    }

   public byte[] generarPdfReporteEventosAdmin(LocalDateTime fechaDesde, LocalDateTime fechaHasta, List<Integer> tagIds) {
    // 1) Obtener eventos filtrados -> reemplazar mocks con query real
    List<AquaEventDTO> eventos = generarEventosMock(fechaDesde, fechaHasta, tagIds); // o consulta real

    // 2) Calcular resumen
    int totalEventos = eventos.size();
    double totalLitros = eventos.stream().mapToDouble(e -> e.getLitrosConsumidos() == null ? 0.0 : e.getLitrosConsumidos()).sum();
    double totalCosto = eventos.stream().mapToDouble(e -> e.getCosto() == null ? 0.0 : e.getCosto()).sum();

    // 3) Calcular ranking por tag (count y avg litros)
    // Usaremos Map<tagNombre, {count, sumLitros, idOptional}>
    Map<String, Integer> counts = new HashMap<>();
    Map<String, Double> sumLitros = new HashMap<>();
    Map<String, Integer> tagIdMap = new HashMap<>();

    for (AquaEventDTO e : eventos) {
        int litros = e.getLitrosConsumidos() == null ? 0 : e.getLitrosConsumidos();
        if (e.getTags() == null) continue;
        for (EventTagDTO t : e.getTags()) {
            String key = t.getNombre() != null ? t.getNombre() : ("tag-" + t.getId());
            counts.put(key, counts.getOrDefault(key, 0) + 1);
            sumLitros.put(key, sumLitros.getOrDefault(key, 0.0) + litros);
            if (t.getId() != null) tagIdMap.put(key, t.getId());
        }
    }

    List<TagRankingDTO> tagRanking = counts.entrySet().stream()
            .map(entry -> {
                String nombre = entry.getKey();
                Integer cnt = entry.getValue();
                Double sum = sumLitros.getOrDefault(nombre, 0.0);
                Double avg = cnt > 0 ? Math.round((sum / cnt) * 100.0) / 100.0 : 0.0;
                Integer id = tagIdMap.get(nombre);
                return new TagRankingDTO(id, nombre, cnt, avg);
            })
            .sorted(Comparator.comparing(TagRankingDTO::getCount, Comparator.nullsLast(Comparator.reverseOrder())))
            .collect(Collectors.toList());

    ResumenEventosDTO resumen = new ResumenEventosDTO(totalEventos,
            Math.round(totalLitros * 100.0) / 100.0,
            Math.round(totalCosto * 100.0) / 100.0,
            tagRanking.size());

    ReporteEventosAdminDTO dto = new ReporteEventosAdminDTO();
    dto.setFechaGeneracion(LocalDateTime.now());
    dto.setFechaDesde(fechaDesde.toLocalDate().toString());
    dto.setFechaHasta(fechaHasta.toLocalDate().toString());
    dto.setResumen(resumen);
    dto.setEventos(eventos);

    // Thymeleaf context
    DateTimeFormatter fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    Context context = new Context();
    context.setVariable("fechaDesde", dto.getFechaDesde());
    context.setVariable("fechaHasta", dto.getFechaHasta());
    context.setVariable("fechaGeneracion", dto.getFechaGeneracion().format(fechaHoraFormatter));
    context.setVariable("resumen", dto.getResumen());
    context.setVariable("eventos", dto.getEventos());
    context.setVariable("tagRanking", tagRanking); // <-- paso el ranking al template

    String html = templateEngine.process("admin-eventos-report", context);

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

// Helpers mocks (reemplazar)
private List<AquaEventDTO> generarEventosMock(LocalDateTime desde, LocalDateTime hasta, List<Integer> tagIds) {
    List<AquaEventDTO> list = new ArrayList<>();
    // ejemplo simple
    AquaEventDTO a = new AquaEventDTO();
    a.setId(1L); a.setTitulo("Riego Jardin"); a.setDescripcion("Riego automatico"); a.setFechaInicio(desde.plusDays(1));
    a.setEstado("COMPLETADO"); a.setLitrosConsumidos(120); a.setCosto(21.6); a.setLocalidad("Palermo"); a.setHogarId(1L);
    a.setTags(Arrays.asList(new EventTagDTO(3,"Riego","#F2C94C")));
    list.add(a);
    // ... agregar más mocks si querés
    return list;
}

private int calcularTagsActivos(List<AquaEventDTO> eventos) {
    Set<Integer> set = new HashSet<>();
    for (AquaEventDTO e : eventos) {
        if (e.getTags() == null) continue;
        for (EventTagDTO t : e.getTags()) set.add(t.getId());
    }
    return set.size();
}


 @Transactional(readOnly = true)
    public List<LocalidadSummaryDTO> getConsumoPorLocalidad(LocalDateTime desde, LocalDateTime hasta) {
        List<Medicion> mediciones = medicionRepository.findAllWithSectorAndHogarBetween(desde, hasta);

        // agrupación por localidad (tomada desde sector.hogar.localidad)
        Map<String, LocalAgg> agg = new HashMap<>();
        for (Medicion m : mediciones) {
            Sector s = m.getSector();
            Hogar h = (s != null) ? s.getHogar() : null;
            String loc = (h != null && h.getLocalidad() != null && !h.getLocalidad().isEmpty())
                    ? h.getLocalidad()
                    : "Sin Localidad";

            LocalAgg a = agg.computeIfAbsent(loc, k -> new LocalAgg());
            double litros = m.getFlow(); 
            a.total += litros;
            a.count++;
            a.costo += litros * 3.0; 
            if (h != null && h.getId() != null) a.hogares.add(h.getId());
        }

        // convertir a DTO
        List<LocalidadSummaryDTO> result = agg.entrySet().stream()
                .map(en -> {
                    String localidad = en.getKey();
                    LocalAgg a = en.getValue();
                    double total = Math.round(a.total * 100.0) / 100.0;
                    double media = a.count > 0 ? Math.round((a.total / a.count) * 100.0) / 100.0 : 0.0;
                    double costo = Math.round(a.costo * 100.0) / 100.0;
                    int hogares = a.hogares.size();
                    return new LocalidadSummaryDTO(localidad, total, media, costo, hogares);
                })
                .sorted(Comparator.comparing(LocalidadSummaryDTO::getTotal, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return result;
    }

    private static class LocalAgg {
        double total = 0.0;
        int count = 0;
        double costo = 0.0;
        Set<Long> hogares = new HashSet<>();
    }


        @Transactional(readOnly = true)
    public byte[] generarPdfReporteLocalidad(LocalDateTime desde, LocalDateTime hasta) {
        List<LocalidadSummaryDTO> resumen = getConsumoPorLocalidad(desde, hasta);
        double totalGlobal = resumen.stream().mapToDouble(r -> r.getTotal() != null ? r.getTotal() : 0.0).sum();

        ReporteLocalidadDTO dto = new ReporteLocalidadDTO();
        dto.setFechaGeneracion(LocalDateTime.now());
        dto.setFechaDesde(desde.toLocalDate().toString());
        dto.setFechaHasta(hasta.toLocalDate().toString());
        dto.setResumenPorLocalidad(resumen);
        dto.setCantidadLocalidades(resumen.size());
        dto.setTotalGlobal(Math.round(totalGlobal * 100.0) / 100.0);

        // Thymeleaf context
        DateTimeFormatter fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Context context = new Context();
        context.setVariable("fechaDesde", dto.getFechaDesde());
        context.setVariable("fechaHasta", dto.getFechaHasta());
        context.setVariable("fechaGeneracion", dto.getFechaGeneracion().format(fechaHoraFormatter));
        context.setVariable("resumenPorLocalidad", dto.getResumenPorLocalidad());
        context.setVariable("totalGlobal", dto.getTotalGlobal());
        context.setVariable("cantidadLocalidades", dto.getCantidadLocalidades());

        String htmlContent = templateEngine.process("admin-localidad-report", context);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.withHtmlContent(htmlContent, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error generando PDF localidad", ex);
        }
    }




}