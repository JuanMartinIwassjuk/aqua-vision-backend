package com.app.aquavision.services;

import com.app.aquavision.dto.proyecciones.*;
import com.app.aquavision.entities.domain.EstadoConsumo;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
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
import java.util.HashMap;
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

    private final double precioPorUnidad = 3;

    private static final Logger logger = LoggerFactory.getLogger(ProyeccionService.class);

    @Transactional(readOnly = true)
    public Hogar findByIdWithSectores(Long id) {
        return hogarRepository.findByIdWithSectores(id)
                .orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado con id: " + id));
    }

    public ProyeccionGraficoHogarDTO generarProyeccionPorHogar(Long hogarId) {
        Hogar hogar = hogarRepository.findByIdWithSectores(hogarId)
                .orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado con id: " + hogarId));

        ProyeccionGraficoHogarDTO proyeccionHogar = new ProyeccionGraficoHogarDTO();

        hogar.getSectores().forEach(sector -> {
            ProyeccionGraficoSectorDTO datosGrafico = generarProyeccionPorSector(sector.getId());
            datosGrafico.setNombreSector(sector.getNombre());
            proyeccionHogar.setHogarId(hogarId);
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

        Map<Integer, Double> consumoHistoricoMap = agruparPorDia(mediciones, inicioMesPasado.getMonthValue(), inicioMesPasado.getYear());
        Map<Integer, Double> consumoActualMap = agruparPorDia(mediciones, hoy.getMonthValue(), hoy.getYear());

        List<Double> ewmaConsumo = calcularEWMA(consumoActualMap, hoy);
        double tendenciaPromedio = calcularTendencia(ewmaConsumo, hoy);

        List<ProyeccionPuntosDTO> puntos = generarPuntosProyeccion(hoy, consumoActualMap, consumoHistoricoMap, ewmaConsumo, tendenciaPromedio);

        double consumoProyectadoTotal = puntos.stream()
                .filter(p -> p.getConsumoProyectado() != null)
                .mapToDouble(ProyeccionPuntosDTO::getConsumoProyectado).sum();

        List<String> hallazgos = analizarConsumo(consumoActualMap, consumoHistoricoMap, consumoProyectadoTotal);

        return new ProyeccionGraficoSectorDTO(puntos, hallazgos);
    }

    private Map<Integer, Double> agruparPorDia(List<Medicion> mediciones, int mes, int anio) {
        return mediciones.stream()
                .filter(m -> m.getTimestamp().toLocalDate().getMonthValue() == mes && m.getTimestamp().toLocalDate().getYear() == anio)
                .collect(Collectors.groupingBy(
                        m -> m.getTimestamp().getDayOfMonth(),
                        Collectors.summingDouble(Medicion::getFlow)
                ));
    }

    private List<Double> calcularEWMA(Map<Integer, Double> consumoActualMap, LocalDate hoy) {
        double alpha = 0.3;
        List<Double> ewmaConsumo = new ArrayList<>();
        double smoothedValue = 0;

        for (int i = 1; i <= hoy.getDayOfMonth(); i++) {
            Double value = consumoActualMap.getOrDefault(i, 0.0);
            if (i == 1) {
                smoothedValue = value;
            } else {
                smoothedValue = alpha * value + (1 - alpha) * smoothedValue;
            }
            ewmaConsumo.add(smoothedValue);
        }
        return ewmaConsumo;
    }

    private double calcularTendencia(List<Double> ewmaConsumo, LocalDate hoy) {
        double tendenciaPromedio = 0;
        int diasParaTendencia = Math.min(hoy.getDayOfMonth() - 1, 7);
        if (diasParaTendencia > 0) {
            double sumaDiferencias = 0;
            for (int i = 0; i < diasParaTendencia; i++) {
                sumaDiferencias += (ewmaConsumo.get(ewmaConsumo.size() - 1 - i) - ewmaConsumo.get(ewmaConsumo.size() - 2 - i));
            }
            tendenciaPromedio = sumaDiferencias / diasParaTendencia;
        }
        return tendenciaPromedio;
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

        Double valorProyectadoAnterior = (hoy.getDayOfMonth() > 0)
                ? ewmaConsumo.get(ewmaConsumo.size() - 1)
                : consumoActualMap.getOrDefault(1, 0.0);

        for (int dia = 1; dia <= diasEnMes; dia++) {
            Double consumoDiaHistorico = consumoHistoricoMap.getOrDefault(dia, 0.0);
            Double consumoDelDia = consumoActualMap.getOrDefault(dia, null);
            Double consumoProyectado;

            if (dia <= hoy.getDayOfMonth()) {
                // ✅ Días pasados o el día actual → usar datos reales
                consumoProyectado = ewmaConsumo.get(dia - 1);
            } else {
                // 🚫 Días futuros → dejar consumo actual nulo para cortar línea
                consumoDelDia = null;

                // Calcular proyección para la línea proyectada
                consumoProyectado = valorProyectadoAnterior + tendenciaPromedio;
                if (consumoProyectado < 0) consumoProyectado = 0.0;

                double factorEstacionalidad =
                        (consumoDiaHistorico - consumoHistoricoMap.getOrDefault(dia - 1, 0.0)) * 0.1;
                consumoProyectado += factorEstacionalidad;
            }

            valorProyectadoAnterior = consumoProyectado;

            Double tendenciaMin = consumoProyectado * (1 - variacion);
            Double tendenciaMax = consumoProyectado * (1 + variacion);

            puntos.add(new ProyeccionPuntosDTO(
                    dia,
                    consumoDiaHistorico,
                    consumoDelDia,       // incluye el día actual, null en días futuros
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
            double consumoProyectadoTotal
    ) {
        List<String> hallazgos = new ArrayList<>();
        LocalDate hoy = LocalDate.now();

        double consumoActualPeriodo = consumoActualMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double consumoHistoricoPeriodo = 0.0;
        for (int i = 1; i <= hoy.getDayOfMonth(); i++) {
            consumoHistoricoPeriodo += consumoHistoricoMap.getOrDefault(i, 0.0);
        }

        double consumoTotalHistorico = consumoHistoricoMap.values().stream().mapToDouble(Double::doubleValue).sum();
        double gastoEstimado = consumoProyectadoTotal * precioPorUnidad;

        // 1️⃣ Tendencia general
        List<Double> consumosOrdenados = consumoActualMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());

        String tendenciaGeneral = "estable";
        if (consumosOrdenados.size() >= 4) {
            double promedioInicio = consumosOrdenados.subList(0, consumosOrdenados.size() / 2)
                    .stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double promedioFinal = consumosOrdenados.subList(consumosOrdenados.size() / 2, consumosOrdenados.size())
                    .stream().mapToDouble(Double::doubleValue).average().orElse(0);

            if (promedioFinal > promedioInicio * 1.15) {
                tendenciaGeneral = "creciente";
            } else if (promedioFinal < promedioInicio * 0.85) {
                tendenciaGeneral = "decreciente";
            }
        }

        switch (tendenciaGeneral) {
            case "creciente" ->
                    hallazgos.add("El consumo general muestra una tendencia creciente en los últimos días.");
            case "decreciente" ->
                    hallazgos.add("El consumo presenta una tendencia descendente, reflejando un mejor control en el uso.");
            default ->
                    hallazgos.add("El consumo se mantiene estable sin grandes variaciones recientes.");
        }

        // 2️⃣ Identificar el pico más alto (consumo absoluto actual)
        int diaMayorConsumo = consumoActualMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
        if (diaMayorConsumo > 0) {
            hallazgos.add("El día " + diaMayorConsumo + " registró el mayor consumo del mes, con un uso de agua elevado.");
        }

        // 3️⃣ Detectar el mayor ahorro relativo (solo días con histórico válido)
        int diaMayorAhorro = -1;
        double mayorAhorroRelativo = 0;
        for (Map.Entry<Integer, Double> entry : consumoActualMap.entrySet()) {
            int dia = entry.getKey();
            double consumo = entry.getValue();
            double consumoHistorico = consumoHistoricoMap.getOrDefault(dia, 0.0);

            if (consumoHistorico > 50) { // evitamos sesgos con históricos muy pequeños
                double variacion = (consumo - consumoHistorico) / consumoHistorico;
                if (variacion < -0.25) {
                    double ahorroRelativo = -variacion;
                    if (ahorroRelativo > mayorAhorroRelativo || (Math.abs(ahorroRelativo - mayorAhorroRelativo) < 0.05 && dia > diaMayorAhorro)) {
                        mayorAhorroRelativo = ahorroRelativo;
                        diaMayorAhorro = dia;
                    }
                }
            }
        }

        if (diaMayorAhorro > 0) {
            if (mayorAhorroRelativo > 0.5) {
                hallazgos.add("El día " + diaMayorAhorro + " fue el más eficiente del mes, con un ahorro destacado respecto al histórico.");
            } else {
                hallazgos.add("El día " + diaMayorAhorro + " presentó un ahorro moderado en comparación con el mes anterior.");
            }
        }

        // 4️⃣ Gasto proyectado
        hallazgos.add("De mantenerse este ritmo, el gasto estimado para el final del mes será de $" + String.format("%.2f", gastoEstimado) + ".");

        // 5️⃣ Situación acumulada
        if (consumoActualPeriodo > consumoTotalHistorico) {
            hallazgos.add("El consumo acumulado ya supera el total del mes pasado, se recomienda revisar posibles fugas o excesos.");
        } else if (consumoActualPeriodo < consumoTotalHistorico * 0.8) {
            hallazgos.add("El consumo acumulado se mantiene por debajo del total del mes pasado, indicando un uso más eficiente.");
        }

        return hallazgos;
    }




    public ProyeccionHogarDTO calcularProyeccion(Long hogarId) {

        YearMonth mesActual = YearMonth.now();
        LocalDateTime inicioMes = mesActual.atDay(1).atStartOfDay();
        LocalDateTime hoy = LocalDate.now().atStartOfDay();
        LocalDateTime finMes = mesActual.atEndOfMonth().atTime(23, 59, 59);

        int diasTranscurridos = LocalDate.now().getDayOfMonth();
        int diasTotalesMes = mesActual.lengthOfMonth();
        int diasRestantes = diasTotalesMes - diasTranscurridos;

        // mostrar las fecha obtenidas
        logger.debug("Iniciando calculo de proyeccion para el hogar con ID: {}", hogarId);
        logger.debug("Inicio del mes: {}", inicioMes);
        logger.debug("Fecha actual: {}", hoy);
        logger.debug("Fin del mes: {}", finMes);
        logger.debug("Días transcurridos: {}", diasTranscurridos);
        logger.debug("Días totales del mes: {}", diasTotalesMes);
        logger.debug("Días restantes: {}", diasRestantes);

        //obtiene el hogar con sus sectores y mediciones de mes actual
        Hogar hogar = reporteService.findByIdWithSectoresAndMediciones(hogarId, inicioMes, finMes);

        // Obtener todos los sectores asociados al hogar en el determiando rango de fechas
        List<Sector> sectores = hogar.getSectores();

        List<ProyeccionSectorDTO> prediccionesSectores = new ArrayList<>();

        for (Sector sector : sectores) {

            float umbralMensual = sector.getUmbralMensual();
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