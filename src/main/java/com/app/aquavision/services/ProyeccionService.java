package com.app.aquavision.services;

import com.app.aquavision.dto.proyecciones.ProyeccionGraficoDTO;
import com.app.aquavision.dto.proyecciones.ProyeccionPuntosDTO;
import com.app.aquavision.dto.proyecciones.ProyeccionHogarDTO;
import com.app.aquavision.dto.proyecciones.ProyeccionSectorDTO;
import com.app.aquavision.entities.domain.EstadoConsumo;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.repositories.HogarRepository;
import com.app.aquavision.repositories.MedicionRepository;
import jakarta.persistence.EntityNotFoundException;
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

    private final double precioPorUnidad = 0.5;

    @Transactional(readOnly = true)
    public Hogar findByIdWithSectores(Long id) {
        return hogarRepository.findByIdWithSectores(id)
                .orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado con id: " + id));
    }

    public Map<String, ProyeccionGraficoDTO> generarProyeccionPorHogar(Long hogarId) {
        Hogar hogar = hogarRepository.findByIdWithSectores(hogarId)
                .orElseThrow(() -> new EntityNotFoundException("Hogar no encontrado con id: " + hogarId));

        Map<String, ProyeccionGraficoDTO> proyeccionPorSector = new HashMap<>();
        hogar.getSectores().forEach(sector -> {
            ProyeccionGraficoDTO datosGrafico = generarProyeccionPorSector(sector.getId());
            proyeccionPorSector.put(sector.getNombre(), datosGrafico);
        });

        return proyeccionPorSector;
    }

    private ProyeccionGraficoDTO generarProyeccionPorSector(Long sectorId) {
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

        return new ProyeccionGraficoDTO(puntos, hallazgos);
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

    private List<ProyeccionPuntosDTO> generarPuntosProyeccion(LocalDate hoy, Map<Integer, Double> consumoActualMap, Map<Integer, Double> consumoHistoricoMap, List<Double> ewmaConsumo, double tendenciaPromedio) {
        List<ProyeccionPuntosDTO> puntos = new ArrayList<>();
        int diasEnMes = hoy.lengthOfMonth();
        double variacion = 0.2;

        Double valorProyectadoAnterior = (hoy.getDayOfMonth() > 0) ? ewmaConsumo.get(ewmaConsumo.size() - 1) : consumoActualMap.getOrDefault(1, 0.0);

        // Total proyectado para hallazgos
        double consumoProyectadoTotal = 0.0;

        for (int dia = 1; dia <= diasEnMes; dia++) {
            Double consumoDiaHistorico = consumoHistoricoMap.getOrDefault(dia, 0.0);
            Double consumoDelDia = consumoActualMap.getOrDefault(dia, null);
            Double consumoProyectado;

            if (dia <= hoy.getDayOfMonth()) {
                // Días pasados y el día actual: usamos el valor suavizado real
                consumoProyectado = ewmaConsumo.get(dia - 1);
            } else {
                // Días futuros: proyectamos el valor anterior más la tendencia
                consumoProyectado = valorProyectadoAnterior + tendenciaPromedio;
                if (consumoProyectado < 0) consumoProyectado = 0.0;

                // ✨ APLICAMOS LA IRREGULARIDAD HISTÓRICA ANTES DE ACTUALIZAR EL VALOR ANTERIOR ✨
                double factorEstacionalidad = (consumoDiaHistorico - consumoHistoricoMap.getOrDefault(dia - 1, 0.0)) * 0.1;
                consumoProyectado += factorEstacionalidad;
            }

            // Actualizamos el valor para la siguiente iteración, ahora con la irregularidad incluida
            valorProyectadoAnterior = consumoProyectado;

            // Sumamos al total proyectado, incluyendo todos los días del mes para el cálculo del gasto
            consumoProyectadoTotal += consumoProyectado;

            Double tendenciaMin = consumoProyectado * (1 - variacion);
            Double tendenciaMax = consumoProyectado * (1 + variacion);

            puntos.add(new ProyeccionPuntosDTO(
                    dia,
                    consumoDiaHistorico,
                    consumoDelDia,
                    consumoProyectado,
                    tendenciaMin,
                    tendenciaMax
            ));
        }

        return puntos;
    }

    private List<String> analizarConsumo(Map<Integer, Double> consumoActualMap, Map<Integer, Double> consumoHistoricoMap, double consumoProyectadoTotal) {
        List<String> hallazgos = new ArrayList<>();

        // Obtenemos el día actual para comparar periodos
        LocalDate hoy = LocalDate.now();

        // 1. Calcular el consumo actual (acumulado hasta hoy)
        double consumoActualPeriodo = consumoActualMap.values().stream().mapToDouble(Double::doubleValue).sum();

        // 2. Calcular el consumo histórico (acumulado hasta el mismo día del mes pasado)
        double consumoHistoricoPeriodo = 0.0;
        for (int i = 1; i <= hoy.getDayOfMonth(); i++) {
            consumoHistoricoPeriodo += consumoHistoricoMap.getOrDefault(i, 0.0);
        }

        // Calcular el consumo total del mes pasado para el análisis final
        double consumoTotalHistorico = consumoHistoricoMap.values().stream().mapToDouble(Double::doubleValue).sum();

        // Cálculo de gastos
        double gastoActual = consumoActualPeriodo * precioPorUnidad;
        double gastoEstimado = consumoProyectadoTotal * precioPorUnidad;

        hallazgos.add("💸 Gasto actual: $" + String.format("%.2f", gastoActual));
        hallazgos.add("💰 Gasto proyectado (fin de mes): $" + String.format("%.2f", gastoEstimado));

        // Análisis de consumo periodo-a-periodo
        if (consumoActualPeriodo > consumoHistoricoPeriodo) {
            hallazgos.add("📈 ¡Cuidado! El consumo actual va " + String.format("%.0f", (consumoActualPeriodo / consumoHistoricoPeriodo - 1) * 100) + "% por encima del mes pasado en esta misma fecha.");
        } else if (consumoActualPeriodo < consumoHistoricoPeriodo) {
            hallazgos.add("📉 ¡Excelente! El consumo de este mes es un " + String.format("%.0f", (1 - consumoActualPeriodo / consumoHistoricoPeriodo) * 100) + "% menor al que llevabas el mes pasado.");
        } else {
            hallazgos.add("📊 El consumo de este mes es similar al que llevabas el mes pasado en este mismo periodo.");
        }

        // Análisis de picos y ahorros diarios
        for (Map.Entry<Integer, Double> entry : consumoActualMap.entrySet()) {
            int dia = entry.getKey();
            double consumo = entry.getValue();
            double consumoHistorico = consumoHistoricoMap.getOrDefault(dia, 0.0);

            if (consumoHistorico > 0 && consumo > consumoHistorico * 1.5) {
                hallazgos.add("🔥 El día " + dia + " hubo un pico de consumo. Fue un " + String.format("%.0f", (consumo / consumoHistorico - 1) * 100) + "% más que en el mes anterior.");
            }

            if (consumoHistorico > 0 && consumo > 0 && consumo < consumoHistorico * 0.75) {
                hallazgos.add("✅ El día " + dia + " se logró un gran ahorro. ¡El consumo fue un " + String.format("%.0f", (1 - consumo / consumoHistorico) * 100) + "% menor que en el mes pasado!");
            }
        }

        // Análisis del consumo acumulado vs. el total del mes pasado
        if (consumoActualPeriodo > consumoTotalHistorico) {
            hallazgos.add("🚨 ¡Atención! El consumo actual (" + String.format("%.2f", consumoActualPeriodo) + ") ya superó el consumo total del mes pasado (" + String.format("%.2f", consumoTotalHistorico) + ").");
        }

        return hallazgos;
    }

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