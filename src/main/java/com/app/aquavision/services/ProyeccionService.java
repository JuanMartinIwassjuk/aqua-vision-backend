package com.app.aquavision.services;

import com.app.aquavision.dto.proyecciones.*;
import com.app.aquavision.entities.domain.EstadoConsumo;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.MedicionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProyeccionService {

    @Autowired
    private MedicionRepository medicionRepository;

    @Autowired
    private ReporteService reporteService;

    @Autowired
    private HogarRepository hogarRepository;

    private static final Logger logger = LoggerFactory.getLogger(ProyeccionService.class);
    private final double precioPorUnidad = 3;

    // -------------------- 🔍 MÉTODOS PRINCIPALES -------------------- //

    @Transactional(readOnly = true)
    public Hogar findByIdWithSectores(Long id) {
        return hogarRepository.findByIdWithSectores(id)
                .orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado con id: " + id));
    }

    public ProyeccionGraficoHogarDTO generarProyeccionPorHogar(Long hogarId) {
        Hogar hogar = hogarRepository.findByIdWithSectores(hogarId)
                .orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado con id: " + hogarId));

        ProyeccionGraficoHogarDTO proyeccionHogar = new ProyeccionGraficoHogarDTO();
        proyeccionHogar.setHogarId(hogarId);

        hogar.getSectores().forEach(sector -> {
            ProyeccionGraficoSectorDTO datosGrafico = generarProyeccionPorSector(sector.getId());
            datosGrafico.setNombreSector(sector.getNombre());
            proyeccionHogar.anadirProyeccionSector(datosGrafico);
        });

        return proyeccionHogar;
    }

    private ProyeccionGraficoSectorDTO generarProyeccionPorSector(Long sectorId) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMesPasado = hoy.minusMonths(1).withDayOfMonth(1);
        LocalDate finMesActual = hoy.withDayOfMonth(hoy.lengthOfMonth());

        List<Medicion> mediciones = medicionRepository.findBySectorIdAndFechaBetween(
                sectorId,
                inicioMesPasado.atStartOfDay(),
                finMesActual.atTime(LocalTime.MAX)
        );

        Map<Integer, Double> consumoHistoricoMap =
                agruparPorDia(mediciones, inicioMesPasado.getMonthValue(), inicioMesPasado.getYear());
        Map<Integer, Double> consumoActualMap =
                agruparPorDia(mediciones, hoy.getMonthValue(), hoy.getYear());

        List<Double> ewmaConsumo = calcularEWMA(consumoActualMap, hoy);
        double tendenciaPromedio = calcularTendencia(ewmaConsumo, hoy);

        List<ProyeccionPuntosDTO> puntos =
                generarPuntosProyeccion(hoy, consumoActualMap, consumoHistoricoMap, ewmaConsumo, tendenciaPromedio);

        // 🔁 Analiza hallazgos con los puntos reales y proyectados
        List<String> hallazgos = analizarConsumo(consumoActualMap, consumoHistoricoMap, puntos);

        return new ProyeccionGraficoSectorDTO(puntos, hallazgos);
    }

    // -------------------- 🧮 CÁLCULOS -------------------- //

    private Map<Integer, Double> agruparPorDia(List<Medicion> mediciones, int mes, int anio) {
        return mediciones.stream()
                .filter(m -> m.getTimestamp().toLocalDate().getMonthValue() == mes
                        && m.getTimestamp().toLocalDate().getYear() == anio)
                .collect(Collectors.groupingBy(
                        m -> m.getTimestamp().getDayOfMonth(),
                        Collectors.summingDouble(Medicion::getFlow)
                ));
    }

    private List<Double> calcularEWMA(Map<Integer, Double> consumoActualMap, LocalDate hoy) {
        double alpha = 0.3;
        List<Double> ewma = new ArrayList<>();
        double smoothedValue = 0;

        for (int i = 1; i <= hoy.getDayOfMonth(); i++) {
            double value = consumoActualMap.getOrDefault(i, 0.0);
            smoothedValue = (i == 1) ? value : alpha * value + (1 - alpha) * smoothedValue;
            ewma.add(smoothedValue);
        }
        return ewma;
    }

    private double calcularTendencia(List<Double> ewmaConsumo, LocalDate hoy) {
        int dias = Math.min(hoy.getDayOfMonth() - 1, 7);
        if (dias <= 1) return 0;

        double suma = 0;
        for (int i = 0; i < dias; i++) {
            int idx = ewmaConsumo.size() - 1 - i;
            if (idx > 0) suma += ewmaConsumo.get(idx) - ewmaConsumo.get(idx - 1);
        }
        return suma / dias;
    }

    private List<ProyeccionPuntosDTO> generarPuntosProyeccion(
            LocalDate hoy,
            Map<Integer, Double> consumoActualMap,
            Map<Integer, Double> consumoHistoricoMap,
            List<Double> ewmaConsumo,
            double tendenciaPromedio
    ) {
        List<ProyeccionPuntosDTO> puntos = new ArrayList<>();
        int diasEnMes = hoy.lengthOfMonth();
        double variacion = 0.2;

        // Promedio de los últimos días reales
        double promedioUltimosDias = consumoActualMap.entrySet().stream()
                .filter(e -> e.getKey() > hoy.getDayOfMonth() - 5)
                .mapToDouble(Map.Entry::getValue)
                .average()
                .orElse(ewmaConsumo.get(ewmaConsumo.size() - 1));

        for (int dia = 1; dia <= diasEnMes; dia++) {
            Double consumoHistorico = consumoHistoricoMap.getOrDefault(dia, 0.0);
            Double consumoActual = consumoActualMap.getOrDefault(dia, null);
            Double consumoProyectado;

            if (dia <= hoy.getDayOfMonth()) {
                consumoProyectado = ewmaConsumo.get(dia - 1);
            } else {
                double factorHistorico = (consumoHistorico > 0 && promedioUltimosDias > 0)
                        ? consumoHistorico / promedioUltimosDias
                        : 1.0;

                double ajusteTendencia = 1 + Math.max(tendenciaPromedio * 0.05, -0.02);
                consumoProyectado = promedioUltimosDias * factorHistorico * ajusteTendencia;
                consumoActual = null;

                if (consumoProyectado < 0) consumoProyectado = 0.0;
            }

            double tendenciaMin = consumoProyectado * (1 - variacion);
            double tendenciaMax = consumoProyectado * (1 + variacion);

            puntos.add(new ProyeccionPuntosDTO(
                    dia,
                    consumoHistorico,
                    consumoActual,
                    consumoProyectado,
                    tendenciaMin,
                    tendenciaMax
            ));
        }

        return puntos;
    }

    private List<String> analizarConsumo(
            Map<Integer, Double> consumoActualMap,
            Map<Integer, Double> consumoHistoricoMap,
            List<ProyeccionPuntosDTO> puntos
    ) {
        List<String> out = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        // Días reales
        List<ProyeccionPuntosDTO> reales = puntos.stream()
                .filter(p -> p.getDia() <= hoy.getDayOfMonth())
                .collect(Collectors.toList());

        double consumoReal = reales.stream()
                .mapToDouble(p -> p.getConsumoActual() != null ? p.getConsumoActual() : 0.0)
                .sum();

        double consumoEstimado = puntos.stream()
                .mapToDouble(p -> p.getConsumoProyectado() != null ? p.getConsumoProyectado() : 0.0)
                .sum();


        // Tendencia general
        List<Double> consumos = reales.stream()
                .map(p -> p.getConsumoActual() != null ? p.getConsumoActual() : 0.0)
                .collect(Collectors.toList());

        String estado = "estable";
        if (consumos.size() > 3) {
            int ventana = Math.min(7, consumos.size());

            double inicio = consumos.subList(0, consumos.size() - ventana)
                    .stream().mapToDouble(Double::doubleValue).average().orElse(0);

            double fin = consumos.subList(consumos.size() - ventana, consumos.size())
                    .stream().mapToDouble(Double::doubleValue).average().orElse(0);

            if (fin > inicio * 1.10) estado = "creciente";
            else if (fin < inicio * 0.90) estado = "decreciente";
        }

        // --- Dashboard compacto ---
        out.add("Estado: " + estado);
        out.add(String.format("Consumo actual: %.2f L", consumoReal));
        out.add(String.format("Proyección mensual: %.2f L", consumoEstimado));


        // Mayor consumo
        consumoActualMap.entrySet().stream()
                .filter(e -> e.getKey() <= hoy.getDayOfMonth())
                .max(Comparator.comparing(Map.Entry::getValue))
                .ifPresent(e -> out.add("Pico de consumo: día " + e.getKey()));

        // Mayor ahorro
        consumoActualMap.entrySet().stream()
                .filter(e -> e.getKey() <= hoy.getDayOfMonth())
                .filter(e -> consumoHistoricoMap.containsKey(e.getKey()))
                .filter(e -> consumoHistoricoMap.get(e.getKey()) > 0)
                .min(Comparator.comparing(e -> e.getValue() / consumoHistoricoMap.get(e.getKey())))
                .ifPresent(e -> {
                    double ahorro = 100 * (1 - e.getValue() / consumoHistoricoMap.get(e.getKey()));
                    out.add(String.format("Ahorro destacado: día %d (%.0f%% menos)", e.getKey(), ahorro));
                });

        return out;
    }


    // -------------------- 📊 PROYECCIÓN GENERAL POR HOGAR -------------------- //

    public ProyeccionHogarDTO calcularProyeccion(Long hogarId) {
        YearMonth mesActual = YearMonth.now();
        LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(hogarId, inicioMes, finMes);
        List<ProyeccionSectorDTO> prediccionesSectores = new ArrayList<>();

        hogar.getSectores().forEach(sector -> {
            List<Medicion> mediciones = medicionRepository.findBySectorIdAndFechaBetween(
                    sector.getId(), inicioMes, hoy.plusDays(1));

            double consumoActualMes = mediciones.stream().mapToDouble(Medicion::getFlow).sum();
            float umbralMensual = sector.getUmbralMensual();

            String tendencia = calcularTendencia(mediciones);
            EstadoConsumo estado = calcularEstadoConsumo(consumoActualMes, umbralMensual);

            ProyeccionSectorDTO dto = new ProyeccionSectorDTO();
            dto.setSectorId(sector.getId());
            dto.setNombreSector(sector.getNombre());
            dto.setConsumoActualMes(consumoActualMes);
            dto.setConsumoProyectadoMes(consumoActualMes); // Se usa valor real aquí
            dto.setTendencia(tendencia);
            dto.setEstadoConsumo(estado);

            prediccionesSectores.add(dto);
        });

        ProyeccionHogarDTO respuesta = new ProyeccionHogarDTO();
        respuesta.setHogarId(hogarId);
        respuesta.setSectores(prediccionesSectores);
        return respuesta;
    }

    private String calcularTendencia(List<Medicion> mediciones) {
        if (mediciones.size() < 2) return "Estable";

        int mitad = mediciones.size() / 2;
        double primeraMitad = mediciones.subList(0, mitad).stream().mapToDouble(Medicion::getFlow).average().orElse(0);
        double segundaMitad = mediciones.subList(mitad, mediciones.size()).stream().mapToDouble(Medicion::getFlow).average().orElse(0);

        if (segundaMitad > primeraMitad * 1.1) return "Creciente";
        if (segundaMitad < primeraMitad * 0.9) return "Decreciente";
        return "Estable";
    }

    private EstadoConsumo calcularEstadoConsumo(double consumoProyectado, double umbralMensual) {
        if (consumoProyectado > umbralMensual * 1.10) return EstadoConsumo.EXCESIVO;
        if (consumoProyectado < umbralMensual * 0.90) return EstadoConsumo.AHORRADOR;
        return EstadoConsumo.NORMAL;
    }
}
