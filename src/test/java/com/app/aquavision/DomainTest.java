package com.app.aquavision;

import com.app.aquavision.entities.domain.*;
import com.app.aquavision.entities.domain.gamification.Recompensa;
import com.app.aquavision.services.notifications.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class DomainTest {

    Hogar hogar = new Hogar(4, "CABA", "aevans@frba.utn.edu.ar");
    LocalDateTime hoy = LocalDateTime.now();

    @Autowired
    EmailService emailService;

    @BeforeEach
    public void init() {

        Medicion medicion1 = new Medicion(100.0, LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        Medicion medicion2 = new Medicion(200.0, hoy);
        Medicion medicion3 = new Medicion(150.0, LocalDateTime.of(2025, 1, 2, 10, 1, 0));
        Medicion medicion4 = new Medicion(300.0, hoy);

        Medidor medidor1 = new Medidor(123456);
        Sector baño = new Sector("Baño", CategoriaSector.BAÑO, medidor1);
        baño.setMediciones(List.of(medicion1, medicion2, medicion3));

        Medidor medidor2 = new Medidor(654321);
        Sector cocina = new Sector("Cocina", CategoriaSector.COCINA, medidor2);
        cocina.setMediciones(List.of(medicion4));

        List<Sector> sectores = List.of(baño, cocina);

        hogar.setSectores(sectores);
    }

    @Test
    void RecompensasTest(){
        Recompensa recompensa = new Recompensa("DESCUENTO MEDIDOR 5%", 100);

        hogar.setPuntos(120);
        hogar.reclamarRecompensa(recompensa);

        assert(
                hogar.getRecompensas().getFirst().getRecompensa().equals(recompensa) &&
                hogar.getPuntos() == 20
        );
    }

    @Test
    void enviarMail(){
        String titulo = " \uD83D\uDEA8 ALERTA!!  \uD83D\uDEA8: Notificación AquaVision";
        String cuerpo = "Este es un mail de prueba, para decirte que el proyecto AquaVision funciona correctamente. \n\nSaludos, \nEl equipo de AquaVision.";
        emailService.enviarMail(hogar.getEmail(),titulo,cuerpo);

        /*
        String aquavision1 = "matiafernandez@frba.utn.edu.ar";
        String aquavision2 = "jiwassjuk@frba.utn.edu.ar";
        String aquavision3 = "mplanchuelo@frba.utn.edu.ar";
        String aquavision4 = "equispechoque@frba.utn.edu.ar";

        emailService.enviarMail(aquavision1,titulo,cuerpo);
        emailService.enviarMail(aquavision2,titulo,cuerpo);
        emailService.enviarMail(aquavision3,titulo,cuerpo);
        emailService.enviarMail(aquavision4,titulo,cuerpo);*/

    }

}
