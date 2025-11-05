package com.app.aquavision.tasks;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.services.HogarService;
import com.app.aquavision.services.notifications.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Logger;

@Component
public class MonitoringTask {

    @Autowired
    private HogarService hogarService;

    @Autowired
    private EmailService emailService;

    private static final Logger logger = Logger.getLogger(MonitoringTask.class.getName());

    // cada hora
    @Scheduled(cron = "0 0 * * * *", zone = "America/Argentina/Buenos_Aires")
    //@Scheduled(cron = "0 * * * * *")
    @Transactional
    public void wasteAnalysis() {

        logger.info("Iniciando tarea de validacion de pérdidas...");

        List<Hogar> hogares = hogarService.findAll();

        for (Hogar hogar : hogares){
            boolean seEnviaAlerta = false;
            StringBuilder cuerpo = new StringBuilder("Validacion de pérdidas / consumos excesivos por sector:\n\n");

            for (Sector sector : hogar.getSectores()){
                if (sector.tienePerdidaAguaReciente()) {

                    cuerpo.append(" - Sector: ").append(sector.getNombre()).append(".\n");

                    cuerpo.append("\n🚨 ALERTA: Se ha detectado una posible fuga de agua en este sector. 🚨\n\n");
                    seEnviaAlerta = true;
                }

            }
            if (seEnviaAlerta){
                emailService.enviarNotificacionFugaAgua(hogar, cuerpo.toString());
                hogarService.save(hogar);
            }
        }

        logger.info("Finalizada tarea de validacion de pérdidas...");
    }

    // 1 vez cada 15 dia a las 3am
    @Scheduled(cron = "0 0 3 15 * ?", zone = "America/Argentina/Buenos_Aires")
    //@Scheduled(cron = "0 * * * * *")
    @Transactional
    public void validateUmbrals() {

        logger.info("Iniciando tarea de validación de umbrales mensuales...");

        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finMes = hoy.withDayOfMonth(hoy.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<Hogar> hogares = hogarService.findAll();

        for (Hogar hogar : hogares){
            boolean seEnviaAlerta = false;
            StringBuilder cuerpo = new StringBuilder("Validacion de proyeccion de sobrepaso de umbrales por sector:\n\n");

            for (Sector sector : hogar.getSectores()){

                float umbralSector = sector.getUmbralMensual();
                float consumoSector = sector.consumoTotalPorFecha(inicioMes, hoy);
                float proyectadoMes = sector.getProyeccionHasta(finMes);

                if (umbralSector < proyectadoMes) {

                    cuerpo.append(" - Sector: ").append(sector.getNombre())
                        .append(" - Consumo actual: ").append(consumoSector)
                        .append(" litros. Umbral mensual: ").append(umbralSector)
                        .append(" litros. Proyección mensual: ").append(proyectadoMes)
                        .append(" litros.\n");

                    cuerpo.append("\n⚠️ ALERTA: Se proyecta que el consumo del sector sobrepasará el umbral mensual establecido. ⚠️\n\n");
                    seEnviaAlerta = true;
                }
            }
            if (seEnviaAlerta){
                emailService.enviarNotificacionSobrepasoUmbrales(hogar, cuerpo.toString());
                hogarService.save(hogar);
            }
        }

        logger.info("Finalizada tarea de validación de umbrales mensuales");
    }

    // El primer día del mes a las 3am
    @Scheduled(cron = "0 0 3 1 * *", zone = "America/Argentina/Buenos_Aires")
    //@Scheduled(cron = "0 * * * * *")
    @Transactional
    public void notificateConsumptions() {

        logger.info("Iniciando tarea de notificación de consumos mensuales...");

        // Calcular el rango del mes anterior
        LocalDate hoy = LocalDate.now();
        LocalDate mesAnterior = hoy.minusMonths(1);
        LocalDateTime inicioMes = mesAnterior.withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = mesAnterior.withDayOfMonth(mesAnterior.lengthOfMonth()).atTime(LocalTime.MAX);

        List<Hogar> hogares = hogarService.findAll();

        for (Hogar hogar : hogares) {
            float consumoHogar = 0f;
            StringBuilder cuerpo = new StringBuilder("Desglose de consumos por sector en el último mes:\n\n");

            for (Sector sector : hogar.getSectores()) {
                float consumoSector = sector.consumoTotalPorFecha(inicioMes, finMes);
                consumoHogar += consumoSector;
                cuerpo.append(" - Sector: ").append(sector.getNombre())
                        .append(" - Consumo: ").append(consumoSector).append(" litros.\n");
            }

            cuerpo.append("\nConsumo total del hogar en el último mes: ").append(consumoHogar).append(" litros.");

            emailService.enviarNotificacionConsumos(hogar, cuerpo.toString());
            hogarService.save(hogar);
        }

        logger.info("Finalizada tarea de envío de consumos mensuales.");
    }


}
