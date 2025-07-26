package com.app.aquavision;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataMock {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @EventListener(ApplicationReadyEvent.class)
  public void generarDatos() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Hogar", Integer.class);
    if (count != null && count > 0) {
      System.out.println("Datos mock ya insertados, no se ejecuta la inserción.");
      return;
    } //TODO: Hay que hacer refactor de esto, la idea es que no reptita la insercion de datos
      //ante cada reinicio de sv, quise implementar algo con sps y que se ejecuten por unica vez
      //pero todavia no salio....

    Random random = new Random();

    //Crear hogares y sectores
    for (int i = 1; i <= 20; i++) {
      jdbcTemplate.update("INSERT INTO Hogar (miembros, localidad) VALUES (?, ?)",
          random.nextInt(5) + 1, "Localidad_" + i);

      int sectoresCount = random.nextInt(3) + 1;
      for (int j = 1; j <= sectoresCount; j++) {
        jdbcTemplate.update("""
                    INSERT INTO Sector (nombre, categoria, hogar_id)
                    VALUES (?, ?, ?)
                """, "Sector_" + i + "_" + j,
            randomCategoria(),
            i);
      }
    }

    //Obtener IDs de sectores una sola vez
    List<Long> sectorIds = jdbcTemplate.query("SELECT id FROM Sector",
        (rs, rowNum) -> rs.getLong("id"));

    //Insertar mediciones por lotes
    int totalMediciones = 100000;
    int batchSize = 5000;
    List<Object[]> batch = new ArrayList<>(batchSize);

    for (int i = 1; i <= totalMediciones; i++) {
      int flow = random.nextInt(100);
      Timestamp ts = Timestamp.valueOf(
          LocalDateTime.now().minusMinutes(random.nextInt(100000)));
      Long sectorId = sectorIds.get(random.nextInt(sectorIds.size()));

      batch.add(new Object[]{flow, ts, sectorId});

      if (i % batchSize == 0) {
        jdbcTemplate.batchUpdate(
            "INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
        batch.clear();
        System.out.println("Insertadas: " + i);
      }
    }

    //último batch si quedó algo pendiente
    if (!batch.isEmpty()) {
      jdbcTemplate.batchUpdate(
          "INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
    }

    System.out.println("Inserts listos");
  }

  private String randomCategoria() {
    return switch (new Random().nextInt(4)) {
      case 0 -> "HOGAR";
      case 1 -> "BAÑO";
      case 2 -> "COCINA";
      default -> "PATIO";
    };
  }
}