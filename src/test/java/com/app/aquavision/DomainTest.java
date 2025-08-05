package com.app.aquavision;

import com.app.aquavision.entities.domain.Categoria;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class DomainTest {

    Hogar hogar = new Hogar(4, "CABA");
    LocalDateTime hoy = LocalDateTime.now();

    @BeforeEach
    public void init() {

        Medicion medicion1 = new Medicion(100, LocalDateTime.of(2025, 1, 1, 10, 0, 0));
        Medicion medicion2 = new Medicion(200, hoy);
        Medicion medicion3 = new Medicion(150, LocalDateTime.of(2025, 1, 2, 10, 1, 0));
        Medicion medicion4 = new Medicion(300, hoy);

        Sector baño = new Sector("Baño", Categoria.BAÑO);
        baño.setMediciones(List.of(medicion1, medicion2, medicion3));

        Sector cocina = new Sector("Cocina", Categoria.COCINA);
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
}
