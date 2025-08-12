package com.app.aquavision.services;

import com.app.aquavision.dto.ConsumoHogarDTO;
import com.app.aquavision.dto.ConsumosPorHoraHogarDTO;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
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


@Service
public class ReporteService {

    @Autowired
    private HogarRepository hogarRepository;
    @Autowired
    private MedicionRepository medicionRepository;

    @Transactional(readOnly = true)
    public Hogar findByIdWithSectoresAndMediciones(Long id, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Hogar hogar = hogarRepository.findByIdWithSectores(id);
        if (hogar == null) return null;

        hogar.getSectores().forEach(sector -> {
            List<Medicion> mediciones = medicionRepository
                    .findBySectorIdAndFechaBetween(sector.getId(), fechaDesde, fechaHasta);
            sector.setMediciones(mediciones);
        });

        return hogar;
    }

    
    @Autowired
    private TemplateEngine templateEngine;



    public byte[] generarPdfReporte(Long hogarId, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogarId, fechaDesde, fechaHasta);
        if (hogar == null) {
            throw new NoSuchElementException("Hogar no encontrado con id: " + hogarId);
        }

        ConsumoHogarDTO dto = new ConsumoHogarDTO(hogar, fechaDesde, fechaHasta);

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