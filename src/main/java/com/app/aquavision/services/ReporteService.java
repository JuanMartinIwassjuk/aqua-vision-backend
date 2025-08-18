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
import java.util.Collections;
import java.util.List;
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

    public ConsumosPorHoraHogarDTO consumosHogarPorHora(Long hogar_id) {

        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyActual = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogar_id, hoyInicio, hoyActual);

        ConsumosPorHoraHogarDTO consumosPorHoraHogarDTO = new ConsumosPorHoraHogarDTO(hogar, hoyInicio, hoyActual);

        for (int i = 0; i < 24; i++) {
            int consumo = hogar.consumoTotalHora(i);
            consumosPorHoraHogarDTO.addConsumoPorHora(new ConsumoPorHoraDTO(i, consumo));
        }

        return consumosPorHoraHogarDTO;
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

    //Exportar Reportes
    @Autowired
    private TemplateEngine templateEngine;

    public byte[] generarPdfReporte(Long hogarId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogarId, fechaDesde, fechaHasta);
        if (hogar == null) {
            throw new NoSuchElementException("Hogar no encontrado con id: " + hogarId);
        }

        ConsumoTotalHogarDTO dto = new ConsumoTotalHogarDTO(hogar, fechaDesde, fechaHasta);

        DateTimeFormatter fechaFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fechaHoraFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Context context = new Context();
        context.setVariable("miembros", dto.getMiembros() != null ? dto.getMiembros() : 0);
        context.setVariable("localidad", dto.getLocalidad() != null ? dto.getLocalidad() : "-");
        context.setVariable("fechaDesde", fechaDesde != null ? fechaDesde.format(fechaFormatter) : "");
        context.setVariable("fechaHasta", fechaHasta != null ? fechaHasta.format(fechaFormatter) : "");
        context.setVariable("fechaGeneracion", dto.getFechaGeneracion() != null ? dto.getFechaGeneracion().format(fechaHoraFormatter) : "");
        context.setVariable("consumoTotal",  dto.getConsumoTotal() != null ? dto.getConsumoTotal() : 0);
        context.setVariable("consumoPromedio", dto.getConsumoPromedio() != null ? dto.getConsumoPromedio() : 0);
        context.setVariable("consumoPico", dto.getConsumoPico() != null ? dto.getConsumoPico() : 0);
        context.setVariable("consumosPorSector", dto.getConsumosPorSector() != null ? dto.getConsumosPorSector() : Collections.emptyList());

        String htmlContent = templateEngine.process("historical-report", context);

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