package com.app.aquavision.tasks;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Sector;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.entities.domain.notifications.TipoNotificacion;
import com.app.aquavision.services.HogarService;
import com.app.aquavision.services.notifications.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Component
public class MonitoringTask { //TODO: Ver qué monitorear, qué notificar y schedules

    @Autowired
    private HogarService hogarService;

    @Autowired
    EmailService emailService;

    private static final Logger logger = Logger.getLogger(MonitoringTask.class.getName());

    public MonitoringTask() {}

    //@Scheduled(cron = "0 */2 * * * ?")
    @Scheduled(cron = "0 0 2 * * ?") // 1 vez por dia a las 3am
    @Transactional
    public void notifySensors() {

        String titulo = " \uD83D\uDEA8 ALERTA!!  \uD83D\uDEA8: Notificación AquaVision";

        logger.info("Iniciando tarea de monitoreo de medidores...");

        List<Hogar> hogares = hogarService.findAll();

        for (Hogar hogar : hogares){
            for (Sector sector : hogar.getSectores()){
                if (sector.getMedidor() != null && !sector.getMedidor().isActive()){
                    String cuerpo = "Tu medidor del sector " + sector.getNombre() + " no se encuentra conectado. \n\nSaludos, \nEl equipo de AquaVision.";
                    Notificacion notificacion = new Notificacion(TipoNotificacion.ALERTA, titulo, cuerpo);
                    emailService.enviarNotificacion(notificacion, hogar.getEmail());
                    hogar.getNotificaciones().add(notificacion);
                    logger.info("Notificación enviada al hogar " + hogar.getId() + ", por medidor inactivo del sector " + sector.getNombre());
                }
            }
            hogarService.save(hogar);
        }

        logger.info("Finalizada tarea de monitoreo de medidores");

    }

    //@Scheduled(cron = "0 */2 * * * ?")
    @Scheduled(cron = "0 0 3 15 * ?") // 1 vez cada 15 dia a las 3am
    @Transactional
    public void validateUmbrals() {
        String titulo = " \uD83D\uDEA8 ALERTA!!  \uD83D\uDEA8: Notificación AquaVision";

        logger.info("Iniciando tarea de validación de umbrales mensuales...");

        LocalDateTime hoy = LocalDateTime.now();
        LocalDateTime inicioMes = hoy.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        List<Hogar> hogares = hogarService.findAll();

        for (Hogar hogar : hogares){
            for (Sector sector : hogar.getSectores()){
                float umbralSector = sector.getUmbralMensual();
                float consumoSector = sector.consumoTotalPorFecha(inicioMes, hoy);

                if (umbralSector < consumoSector*2) //TODO: Hacerlo más preciso
                {
                    String cuerpo = "Tu umbral del sector " + sector.getNombre() + " es muy probable que se sobrepase. \n\nSaludos, \nEl equipo de AquaVision.";
                    Notificacion notificacion = new Notificacion(TipoNotificacion.ALERTA, titulo, cuerpo);
                    emailService.enviarNotificacion(notificacion, hogar.getEmail());
                    hogar.getNotificaciones().add(notificacion);
                    logger.info("Notificación enviada al hogar " + hogar.getId() + " por umbral mensual del sector " + sector.getNombre());
                }
            }
            hogarService.save(hogar);
        }

        logger.info("Finalizada tarea de validación de umbrales mensuales");
    }

}
