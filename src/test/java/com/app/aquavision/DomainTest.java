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

        Medicion medicion1 = new Medicion(100, LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        Medicion medicion2 = new Medicion(200, hoy);
        Medicion medicion3 = new Medicion(150, LocalDateTime.of(2025, 1, 2, 10, 1, 0));
        Medicion medicion4 = new Medicion(300, hoy);

        Medidor medidor1 = new Medidor(123456);
        Sector baño = new Sector("Baño", Categoria.BAÑO, medidor1);
        baño.setMediciones(List.of(medicion1, medicion2, medicion3));

        Medidor medidor2 = new Medidor(654321);
        Sector cocina = new Sector("Cocina", Categoria.COCINA, medidor2);
        cocina.setMediciones(List.of(medicion4));

        List<Sector> sectores = List.of(baño, cocina);

        hogar.setSectores(sectores);
    }

    @Test
    void HogarTest() {
        hogar.mostrarReporteHogar();
    }

    @Test
    void ConsumoHogarActualTest() {
        hogar.mostrarConsumoActualDiaro();
    }

    @Test
    void ConsumoHogarPorFechasTest() {
        LocalDateTime fechaInicio = LocalDateTime.of(2025, 1, 5, 0, 0, 0);
        hogar.mostrarConsumoTotalPorFechas(fechaInicio, hoy);
    }

    @Test
    void ProyeccionHogarMensualTest() {
        hogar.mostarProyeccionConsumoMensual();
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
        emailService.enviarMail(hogar.getEmail(), "Test", "Este es un mail de prueba");
    }

}
