package com.app.aquavision;

import com.app.aquavision.entities.domain.Categoria;
import com.app.aquavision.entities.domain.Hogar;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Sector;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class DomainTest {

    @Test
    void HogarTest() {

        Medicion medicion1 = new Medicion(100, LocalDateTime.of(2025,1,1,10,0,0));
        Medicion medicion2 = new Medicion(200, LocalDateTime.of(2025,1,2,10,0,0));
        Medicion medicion3 = new Medicion(150, LocalDateTime.of(2025,1,2,10,1,0));
        Medicion medicion4 = new Medicion(300, LocalDateTime.of(2025,1,3,10,0,0));

        Sector baño = new Sector("Baño", Categoria.BAÑO);
        baño.setMediciones(List.of(medicion1, medicion2, medicion3));

        Sector cocina = new Sector("Cocina", Categoria.COCINA);
        cocina.setMediciones(List.of(medicion4));

        List<Sector> sectores = List.of(baño, cocina);
        Hogar hogar = new Hogar(4,"CABA", sectores);

        hogar.mostrarReporteHogar();

    }

}
