package com.app.aquavision;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class DataMock {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @EventListener(ApplicationReadyEvent.class)
  public void generarDatos() {
    System.out.println("Comienzo de insercion de datos");
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Hogar", Integer.class);
    if (count != null && count > 0) {
      System.out.println("Datos mock ya insertados, no se ejecuto la inserción.");
      return;
    }

    Random random = new Random();


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


    List<Long> sectorIds = jdbcTemplate.query("SELECT id FROM Sector",
        (rs, rowNum) -> rs.getLong("id"));


    int totalMediciones = 15000;
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

    if (!batch.isEmpty()) {
      jdbcTemplate.batchUpdate(
          "INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
    }


    insertarRoles();
    insertarUsuarios();

    System.out.println("Inserts listos");
  }

  private void insertarRoles() {
    List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
    for (String rol : roles) {
      Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Role_ WHERE name = ?", Integer.class, rol);
      if (count == 0) {
        jdbcTemplate.update("INSERT INTO Role_ (name) VALUES (?)", rol);
      }
    }
  }

  private void insertarUsuarios() {
    Map<String, Boolean> usuarios = new LinkedHashMap<>();
    usuarios.put("matif", false);
    usuarios.put("matifadmin", true);
    usuarios.put("matip", false);
    usuarios.put("matipadmin", true);
    usuarios.put("erik", false);
    usuarios.put("erikadmin", true);
    usuarios.put("juan", false);
    usuarios.put("juanadmin", true);
    usuarios.put("agus", false);
    usuarios.put("agusadmin", true);

    for (int i = 1; i <= 10; i++) {
      usuarios.put("user" + i, false);
    }

    Long roleUserId = jdbcTemplate.queryForObject("SELECT id FROM Role_ WHERE name = 'ROLE_USER'", Long.class);
    Long roleAdminId = jdbcTemplate.queryForObject("SELECT id FROM Role_ WHERE name = 'ROLE_ADMIN'", Long.class);

    List<Long> hogaresDisponibles = jdbcTemplate.query("SELECT id FROM Hogar",
        (rs, rowNum) -> rs.getLong("id"));

    if (hogaresDisponibles.size() < usuarios.size()) {
      throw new IllegalStateException("No hay suficientes hogares para asignar a todos los usuarios");
    }

    int index = 0;
    for (Map.Entry<String, Boolean> entry : usuarios.entrySet()) {
      String username = entry.getKey();
      boolean esAdmin = entry.getValue();
      Long hogarId = hogaresDisponibles.get(index++);

      Integer existe = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM User_ WHERE username = ?", Integer.class, username);
      if (existe != null && existe > 0) continue;

      jdbcTemplate.update("INSERT INTO User_ (username, password, enabled, hogar_id) VALUES (?, ?, ?, ?)",
          username, passwordEncoder.encode("test123"), true, hogarId);

      Long userId = jdbcTemplate.queryForObject("SELECT id FROM User_ WHERE username = ?", Long.class, username);
      jdbcTemplate.update("INSERT INTO Usuarios_Roles (user_id, role_id) VALUES (?, ?)",
          userId, esAdmin ? roleAdminId : roleUserId);
    }
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