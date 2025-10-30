package com.app.aquavision.services;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;


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


}