package com.app.aquavision.services;

import com.app.aquavision.dto.admin.consumo.ConsumoDiaDTO;
import com.app.aquavision.dto.admin.consumo.HogarConsumoDTO;
import com.app.aquavision.dto.admin.consumo.ReporteConsumoAdminDTO;
import com.app.aquavision.dto.admin.consumo.ResumenConsumoGlobalDTO;
import com.app.aquavision.dto.admin.consumo.TopMesDTO;
import com.app.aquavision.dto.admin.eventos.AquaEventDTO;
import com.app.aquavision.dto.admin.eventos.EventTagDTO;
import com.app.aquavision.dto.admin.eventos.ReporteEventosAdminDTO;
import com.app.aquavision.dto.admin.eventos.ResumenEventosDTO;
import com.app.aquavision.dto.admin.eventos.TagRankingDTO;
import com.app.aquavision.dto.admin.gamification.HogarRankingDTO;
import com.app.aquavision.dto.admin.gamification.MedallasHogarDTO;
import com.app.aquavision.dto.admin.gamification.PuntosDiaDTO;
import com.app.aquavision.dto.admin.gamification.ResumenGamificacionDTO;
import com.app.aquavision.dto.admin.localidad.LocalidadSummaryDTO;
import com.app.aquavision.dto.admin.localidad.ReporteLocalidadDTO;
import com.app.aquavision.dto.consumos.*;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.entities.domain.gamification.PuntosReclamados;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.MedicionRepository;
import com.app.aquavision.repositories.PuntosReclamadosRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.Duration;
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
import java.util.TreeMap;
import java.util.stream.Collectors;


@Service
public class ReporteService {

    private final double costoPorPunto = 1.0;

    private  final double COSTO_POR_LITRO = 0.18; // o el real que uses

    @Autowired
    private  PuntosReclamadosRepository puntosReclamadosRepository;

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
                            //.mapToInt(ConsumoTotalSectorDTO::getConsumoPico)
                            .mapToDouble(ConsumoTotalSectorDTO::getConsumoPico)
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
        //float pico = s.picoConsumo();
        Double pico = s.picoConsumo();
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

    LocalDate d = fechaDesde.toLocalDate();
    LocalDate h = fechaHasta.toLocalDate();

    ResumenConsumoGlobalDTO resumen = getResumen(d, h);
    List<HogarConsumoDTO> hogares = getConsumoPorHogar(d, h);

    ReporteConsumoAdminDTO dto = new ReporteConsumoAdminDTO(
            LocalDateTime.now(),
            d.toString(),
            h.toString(),
            resumen,
            hogares
    );

    Context context = new Context();
    context.setVariable("fechaDesde", dto.getFechaDesde());
    context.setVariable("fechaHasta", dto.getFechaHasta());
    context.setVariable("fechaGeneracion", dto.getFechaGeneracion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    context.setVariable("resumen", dto.getResumen());
    context.setVariable("hogares", dto.getHogares());

    String html = templateEngine.process("admin-consumo-report", context);

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

    @Transactional(readOnly = true)
      public List<PuntosDiaDTO> getPuntosPorPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) return Collections.emptyList();

        // rango inclusive: desde 00:00:00 hasta 23:59:59.999
        LocalDateTime from = desde.atStartOfDay();
        LocalDateTime to = hasta.atTime(23, 59, 59, 999_999_999);

        List<PuntosReclamados> rows = puntosReclamadosRepository.findByFechaBetween(from, to);

        // Agrupar por fecha (yyyy-MM-dd) y sumar puntos
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Map<String, Long> byDay = rows.stream()
            .filter(r -> r.getFecha() != null)
            .collect(Collectors.groupingBy(
                r -> r.getFecha().toLocalDate().format(fmt),
                Collectors.summingLong(r -> r.getPuntos())
            ));

        // Queremos devolver una lista ordenada por fecha asc
        List<String> orderedDates = new ArrayList<>(byDay.keySet());
        orderedDates.sort(Comparator.naturalOrder());

        List<PuntosDiaDTO> result = new ArrayList<>();
        for (String date : orderedDates) {
            result.add(new PuntosDiaDTO(date, byDay.getOrDefault(date, 0L)));
        }

        return result;
    }

    // --- Resumen gamificacion ---
    @Transactional(readOnly = true)
    public ResumenGamificacionDTO getResumenGamificacion(LocalDate desde, LocalDate hasta) {
        List<Hogar> hogares = hogarRepository.findAll();
        long total = hogares.stream().mapToLong(h -> (long) h.getPuntos()).sum();

        long dias = Duration.between(desde.atStartOfDay(), hasta.atTime(LocalTime.MAX)).toDays() + 1;
        double media = dias > 0 ? Math.round(((double) total / dias) * 10.0) / 10.0 : 0.0;

        int mejorRacha = hogares.stream().mapToInt(h -> h.getRachaDiaria()).max().orElse(0);
        return new ResumenGamificacionDTO(total, media, mejorRacha);
    }

    // --- Ranking por puntos (usa puntosDisponibles) ---
    @Transactional(readOnly = true)
    public List<HogarRankingDTO> getRankingPuntos(LocalDate desde, LocalDate hasta) {
        return hogarRepository.findAll().stream()
            .map(h -> new HogarRankingDTO(h.getId(), h.getNombre(), h.getPuntos(), 0.0, h.getRachaDiaria()))
            .sorted(Comparator.comparingLong(HogarRankingDTO::getPuntos).reversed())
            .collect(Collectors.toList());
    }

    // --- Ranking por rachas ---
    @Transactional(readOnly = true)
    public List<HogarRankingDTO> getRankingRachas(LocalDate desde, LocalDate hasta) {
        return hogarRepository.findAll().stream()
            .map(h -> new HogarRankingDTO(h.getId(), h.getNombre(), h.getPuntos(), 0.0, h.getRachaDiaria()))
            .sorted(Comparator.comparingInt(HogarRankingDTO::getRacha).reversed())
            .collect(Collectors.toList());
    }

    // --- listado simple de hogares (para front) ---
    @Transactional(readOnly = true)
    public List<HogarRankingDTO> getHogaresSummary() {
        return hogarRepository.findAll().stream()
                .map(h -> new HogarRankingDTO(h.getId(), h.getNombre(), h.getPuntos(), 0.0, h.getRachaDiaria()))
                .collect(Collectors.toList());
    }

    // --- Medallas por hogar (usa la relación ManyToMany) ---
    @Transactional(readOnly = true)
    public MedallasHogarDTO getMedallasPorHogar(Long hogarId) {
        Optional<Hogar> oh = hogarRepository.findById(hogarId);
        if (oh.isEmpty()) return new MedallasHogarDTO(hogarId, "—", Collections.emptyList());
        Hogar h = oh.get();
        List<String> medallas = h.getMedallas().stream()
                .map(m -> m.getNombre()) // tu entity Medalla tiene getNombre()
                .collect(Collectors.toList());
        return new MedallasHogarDTO(h.getId(), h.getNombre(), medallas);
    }

    // --- Generar PDF igual que antes pero usando los nuevos métodos ---
    @Transactional(readOnly = true)
    public byte[] generarPdfReporteGamificacion(LocalDate desde, LocalDate hasta) {
        List<PuntosDiaDTO> puntos = getPuntosPorPeriodo(desde, hasta);
        ResumenGamificacionDTO resumen = getResumenGamificacion(desde, hasta);
        List<HogarRankingDTO> topPuntos = getRankingPuntos(desde, hasta).stream().limit(20).collect(Collectors.toList());
        List<HogarRankingDTO> topRachas = getRankingRachas(desde, hasta).stream().limit(20).collect(Collectors.toList());

        Context context = new Context();
        DateTimeFormatter fh = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        context.setVariable("fechaDesde", desde.toString());
        context.setVariable("fechaHasta", hasta.toString());
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(fh));
        context.setVariable("puntosPorDia", puntos);
        context.setVariable("resumen", resumen);
        context.setVariable("topPuntos", topPuntos);
        context.setVariable("topRachas", topRachas);

        String html = templateEngine.process("admin-gamificacion-report", context);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.withHtmlContent(html, "");
            builder.toStream(baos);
            builder.run();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF gamificación", e);
        }
    }


   public List<ConsumoDiaDTO> getConsumoGlobalPorPeriodo(LocalDate desde, LocalDate hasta) {

    LocalDateTime d = desde.atStartOfDay();
    LocalDateTime h = hasta.atTime(23, 59, 59);

    List<Object[]> rows = medicionRepository.sumFlowGroupByDay(d, h);

    return rows.stream().map(r -> new ConsumoDiaDTO(
            ((java.sql.Date) r[0]).toLocalDate(),                
            ((Number) r[1]).doubleValue(),     
            0.0,                                 
            0.0                                  
    )).toList();
}



    public ResumenConsumoGlobalDTO getResumen(LocalDate desde, LocalDate hasta) {

        LocalDateTime d = desde.atStartOfDay();
        LocalDateTime h = hasta.atTime(23, 59, 59);

        List<Object[]> rows = medicionRepository.sumFlowGroupByDay(d, h);

        double total = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).sum();
        double pico  = rows.stream().mapToDouble(r -> ((Number) r[1]).doubleValue()).max().orElse(0);
        double media = rows.isEmpty() ? 0 : total / rows.size();
        double costo = total * COSTO_POR_LITRO;

        return new ResumenConsumoGlobalDTO(
                round(total), round(media), round(pico), round(costo)
        );
    }


    public List<HogarConsumoDTO> getConsumoPorHogar(LocalDate desde, LocalDate hasta) {

        LocalDateTime d = desde.atStartOfDay();
        LocalDateTime h = hasta.atTime(23, 59, 59);

        List<Object[]> rows = medicionRepository.consumoPorHogar(d, h);

        return rows.stream().map(r -> new HogarConsumoDTO(
                ((Number) r[0]).longValue(),  // id
                (String) r[1],                // nombre
                (String) r[2],                // localidad
                ((Number) r[3]).intValue(),   // integrantes
                ((Number) r[4]).doubleValue(), // total litros
                round(((Number) r[4]).doubleValue() * COSTO_POR_LITRO)
        )).toList();
    }


    public List<TopMesDTO> getTopMeses(LocalDate desde, LocalDate hasta) {
        LocalDateTime d = desde.atStartOfDay();
        LocalDateTime h = hasta.atTime(23, 59, 59);

        List<Object[]> rows = medicionRepository.topMeses(d, h);

        return rows.stream().map(r -> new TopMesDTO(
                ((Number) r[0]).intValue(),         // mes
                ((Number) r[1]).doubleValue(),       // total
                round(((Number) r[1]).doubleValue() / 30) // media aproximada
        )).toList();
    }


    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

}