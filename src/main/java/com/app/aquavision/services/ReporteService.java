package com.app.aquavision.services;

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



    public byte[] generarPdfReporte(Long hogarId) {
        LocalDateTime hoyInicio = LocalDate.now().atStartOfDay();
        LocalDateTime hoyActual = LocalDate.now().atTime(LocalTime.MAX);

        Hogar hogar = this.findByIdWithSectoresAndMediciones(hogarId, hoyInicio, hoyActual);
        if (hogar == null) {
            throw new NoSuchElementException("Hogar no encontrado con id: " + hogarId);
        }

        ConsumosPorHoraHogarDTO dto = new ConsumosPorHoraHogarDTO(hogar, hoyInicio, hoyActual);

        Context context = new Context();
        context.setVariable("hogarId", dto.getHogarId());
        context.setVariable("miembros", dto.getMiembros());
        context.setVariable("localidad", dto.getLocalidad());
        context.setVariable("consumosPorHora", dto.getConsumosPorHora());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        context.setVariable("fechaGeneracion", LocalDateTime.now().format(formatter));

 
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