package com.app.aquavision;

import com.app.aquavision.entities.domain.EstadoMedidor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger logger = LoggerFactory.getLogger(DataMock.class);

  @EventListener(ApplicationReadyEvent.class)
  public void generarDatos() {

    if (this.datosMockInsertados()) {
      logger.info("Datos mock ya insertados, no se ejecutará la inserción.");
      return;
    }

    logger.info("Comienzo de inserción de datos mock");

    insertarHogares();
    insertarMediciones();
    insertarRecompensas();

    insertarRoles();
    insertarUsuarios();

    logger.info("Datos mock insertados correctamente");

  }

  private void insertarHogares() {

      logger.info("Insertando hogares, sectores y medidores...");

        int medidorId = 1;
        int cantidadHogares = 20;
        Random random = new Random();

        for (int hogarId = 1; hogarId <= cantidadHogares; hogarId++) {

            int cantidadMiembros = random.nextInt(5) + 1;
            int cantidadSectores = random.nextInt(3) + 1;

            jdbcTemplate.update("INSERT INTO Hogar (miembros, localidad, email, racha_diaria, puntos) VALUES (?, ?, ?, 0, 0)",
                    cantidadMiembros, randomLocalidad(), "hogar" + hogarId + "@example.com");

            List<String> categorias = getCategorias(cantidadSectores);
            for (int j = 0; j < cantidadSectores; j++) {

                String categoria = categorias.get(j);

                //Insert medidor
                int numeroSerie = 100000 + hogarId * 1000 + j;
                jdbcTemplate.update("INSERT INTO medidores (numero_serie, estado) VALUES (?, ?)",
                        numeroSerie, EstadoMedidor.ON.name());

                //Insert sector
                jdbcTemplate.update("INSERT INTO Sector (nombre, categoria_sector, hogar_id, medidor_id) VALUES (?, ?, ?, ?)",
                categoria, categoria, hogarId, medidorId);

                medidorId++;
            }
        }

  }

  private void insertarMediciones(){

      logger.info("Insertando mediciones...");

      Random random = new Random();

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
              logger.info("Insertadas {} mediciones", i);
          }
      }

      if (!batch.isEmpty()) {
          jdbcTemplate.batchUpdate(
                  "INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
      }
  }

  private void insertarRecompensas(){

    logger.info("Insertando recompensas...");

    List<Map<String, Object>> recompensas = List.of(
        Map.of("descripcion", "Descuento del 10% en medidor", "puntos", 50),
        Map.of("descripcion", "Descuento del 20% en medidor", "puntos", 90),
        Map.of("descripcion", "Descuento del 5% en mantenimiento", "puntos", 300)
    );

    for (Map<String, Object> recompensa : recompensas) {
      String descripcion = (String) recompensa.get("descripcion");
      int puntos = (int) recompensa.get("puntos");

      Integer count = jdbcTemplate.queryForObject(
          "SELECT COUNT(*) FROM Recompensa WHERE descripcion = ?", Integer.class, descripcion);
      if (count == 0) {
        jdbcTemplate.update(
            "INSERT INTO Recompensa (descripcion, puntos_necesarios) VALUES (?, ?)",
            descripcion, puntos);
      }
    }
  }

  private void insertarRoles() {

    logger.info("Insertando roles...");

    List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
    for (String rol : roles) {
      Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Role_ WHERE name = ?", Integer.class, rol);
      if (count == 0) {
        jdbcTemplate.update("INSERT INTO Role_ (name) VALUES (?)", rol);
      }
    }
  }

  private void insertarUsuarios() {

    logger.info("Insertando usuarios...");

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
      if (existe > 0) continue;

      jdbcTemplate.update("INSERT INTO User_ (username, password, enabled, hogar_id) VALUES (?, ?, ?, ?)",
          username, passwordEncoder.encode("test123"), true, hogarId);

      Long userId = jdbcTemplate.queryForObject("SELECT id FROM User_ WHERE username = ?", Long.class, username);
      jdbcTemplate.update("INSERT INTO Usuarios_Roles (user_id, role_id) VALUES (?, ?)",
          userId, esAdmin ? roleAdminId : roleUserId);
    }
  }

  private List<String> getCategorias(int cantidad) {

      List<String> categorias = new ArrayList<>(List.of("BAÑO", "COCINA", "PATIO", "LAVADERO"));

      if (cantidad == 1){
          return new ArrayList<>(List.of("HOGAR"));
      }

      for (int i = categorias.size(); i < cantidad; i++){
          categorias.add("BAÑO");
      }
      return categorias;

  }

  private String randomLocalidad() {
    return switch (new Random().nextInt(5)) {
      case 0 -> "Palermo";
      case 1 -> "Belgrano";
      case 2 -> "Recoleta";
      case 3 -> "Caballito";
      default -> "CABA";
    };
  }

  private boolean datosMockInsertados() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Hogar", Integer.class);
    return count != null && count > 0;
  }

}