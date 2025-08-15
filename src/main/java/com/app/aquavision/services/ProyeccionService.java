package com.app.aquavision.services;

import com.app.aquavision.dto.proyecciones.ProyeccionHogarDTO;
import com.app.aquavision.dto.proyecciones.ProyeccionSectorDTO;
import com.app.aquavision.entities.domain.EstadoConsumo;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.repositories.MedicionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProyeccionService {

    @Autowired
    private MedicionRepository medicionRepository;
    @Autowired
    private ReporteService reporteService;

    public ProyeccionHogarDTO calcularProyeccion(Long hogarId, double umbralMensual) {

        YearMonth mesActual = YearMonth.now();
        LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

        int diasTranscurridos = LocalDate.now().getDayOfMonth();
        int diasTotalesMes = mesActual.lengthOfMonth();
        int diasRestantes = diasTotalesMes - diasTranscurridos;


        // mostrar las fecha obtenidas
        System.out.println("Inicio del mes: " + inicioMes);
        System.out.println("Fecha actual: " + hoy);
        System.out.println("Fin del mes: " + finMes);
        System.out.println("Días transcurridos: " + diasTranscurridos);
        System.out.println("Días totales del mes: " + diasTotalesMes);
        System.out.println("Días restantes: " + diasRestantes);


        //obtiene el hogar con sus sectores y mediciones de mes actual
        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(hogarId, inicioMes, finMes);

        // Obtener todos los sectores asociados al hogar en el determiando rango de fechas
        List<Sector> sectores = hogar.getSectores();

        List<ProyeccionSectorDTO> prediccionesSectores = new ArrayList<>();

        for (Sector sector : sectores) {
            // Obtener mediciones de este sector en el mes actual-las mediciones hasta hoy
            List<Medicion> mediciones = medicionRepository.findBySectorIdAndFechaBetween(
                    sector.getId(), inicioMes, hoy.plusDays(1)
            );

            double consumoActualMes = mediciones.stream()
                    .mapToDouble(Medicion::getFlow)
                    .sum();

            // Si no hay mediciones, proyectamos 0
            if (mediciones.isEmpty()) {
                prediccionesSectores.add(new ProyeccionSectorDTO(
                        sector.getId(),
                        sector.getNombre(),
                        0.0,
                        0.0,
                        "Estable",
                        EstadoConsumo.NORMAL
                ));
                continue;
            }

            // Promedio diario y proyección
            double promedioDiario = consumoActualMes / diasTranscurridos;

            // Proyección del consumo para el resto del mes
            double consumoProyectado = consumoActualMes + (promedioDiario * diasRestantes);

            // Calcular tendencia del consumo
            String tendencia = calcularTendencia(mediciones);
            //calculo del estado de consumo
            EstadoConsumo estado = calcularEstadoConsumo(consumoProyectado, umbralMensual);


            // Agregar DTO de predicción por sector
            ProyeccionSectorDTO dto = new ProyeccionSectorDTO();
            dto.setSectorId(sector.getId());
            dto.setNombreSector(sector.getNombre());
            dto.setConsumoActualMes(consumoActualMes);
            dto.setConsumoProyectadoMes(consumoProyectado);
            dto.setTendencia(tendencia);
            dto.setEstadoConsumo(estado);

            prediccionesSectores.add(dto);
        }

        // DTO final del hogar
        ProyeccionHogarDTO respuesta = new ProyeccionHogarDTO();
        respuesta.setHogarId(hogarId);
        respuesta.setSectores(prediccionesSectores);

        return respuesta;
    }

    /**
     * Calcula si el consumo está en tendencia creciente, decreciente o estable.
     */
    private String calcularTendencia(List<Medicion> mediciones) {
        if (mediciones.size() < 2) return "Estable";

        int mitad = mediciones.size() / 2;

        double primeraMitad = mediciones.subList(0, mitad)
                .stream().mapToDouble(Medicion::getFlow).average().orElse(0);
        double segundaMitad = mediciones.subList(mitad, mediciones.size())
                .stream().mapToDouble(Medicion::getFlow).average().orElse(0);

        if (segundaMitad > primeraMitad * 1.1) return "Creciente";
        if (segundaMitad < primeraMitad * 0.9) return "Decreciente";
        return "Estable";
    }

    /**
     * Calcula el estado de consumo basado en la proyección y un umbral, devolviendo un enum.
     */
    private EstadoConsumo calcularEstadoConsumo(double consumoProyectado, double umbralMensual) {
        if (consumoProyectado > umbralMensual * 1.10) {
            return EstadoConsumo.EXCESIVO;
        }
        if (consumoProyectado < umbralMensual * 0.90) {
            return EstadoConsumo.AHORRADOR;
        }
        return EstadoConsumo.NORMAL;
    }
}