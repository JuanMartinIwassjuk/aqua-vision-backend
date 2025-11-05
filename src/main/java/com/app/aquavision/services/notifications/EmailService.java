package com.app.aquavision.services.notifications;

import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.entities.domain.notifications.TipoNotificacion;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarMail(String destinatario, String asunto, String mensaje) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(destinatario);
        email.setSubject(asunto);
        email.setText(mensaje);
        mailSender.send(email);
        logger.info("Email enviado a: " + destinatario + ". Asunto: " + asunto);
    }

    public void enviarNotificacion(Notificacion notificacion, String destinatario) {
        this.enviarMail(destinatario, notificacion.getTitulo(), notificacion.getMensaje());
    }

    public void enviarNotificacionConsumos(Hogar hogar, String cuerpo) {
        String titulo = "📊💧 Informe de Consumo Mensual - AquaVision 💧📊";
        Notificacion notificacion = new Notificacion(TipoNotificacion.INFORME, titulo, cuerpo);

        this.enviarMail(hogar.getEmail(), titulo, cuerpo);
        hogar.agregarNotificacion(notificacion);
        notificacion.setHogar(hogar);
    }

    public void enviarNotificacionSobrepasoUmbrales(Hogar hogar, String cuerpo) {
        String titulo = "⚠️ Alerta de validacion de umbrales - AquaVision ⚠️";
        Notificacion notificacion = new Notificacion(TipoNotificacion.ALERTA, titulo, cuerpo);

        this.enviarMail(hogar.getEmail(), titulo, cuerpo);
        hogar.agregarNotificacion(notificacion);
        notificacion.setHogar(hogar);
    }

    public void enviarNotificacionSensorInactivo(Hogar hogar, String cuerpo) {
        String titulo = "🚨 Alerta de sensor inactivo - AquaVision 🚨";
        Notificacion notificacion = new Notificacion(TipoNotificacion.ALERTA, titulo, cuerpo);

        this.enviarMail(hogar.getEmail(), titulo, cuerpo);
        hogar.agregarNotificacion(notificacion);
        notificacion.setHogar(hogar);
    }

    public void enviarNotificacionFugaAgua(Hogar hogar, String cuerpo) {
        String titulo = "🚨💧 Alerta de fuga de agua detectada - AquaVision 💧🚨";
        Notificacion notificacion = new Notificacion(TipoNotificacion.ALERTA, titulo, cuerpo);

        this.enviarMail(hogar.getEmail(), titulo, cuerpo);
        hogar.agregarNotificacion(notificacion);
        notificacion.setHogar(hogar);
    }


}
