package com.app.aquavision.boostrap;

import com.app.aquavision.entities.User;
import com.app.aquavision.entities.domain.EstadoMedidor;
import com.app.aquavision.entities.domain.gamification.Minijuego;
import com.app.aquavision.entities.domain.notifications.TipoNotificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
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
@Order(1)
public class DataMock {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final int cantidadHogares = 7;

    private static final Logger logger = LoggerFactory.getLogger(DataMock.class);

    @EventListener(ApplicationReadyEvent.class)
    public void generarDatos() {

        if (this.datosMockInsertados()) {
            logger.info("Datos mock ya insertados, no se ejecutará la inserción.");
            return;
        }

        logger.info("Comienzo de inserción de datos mock");

        insertarLogrosYMedallas();
        insertarPlanes();

        insertarHogares();
        insertarMediciones();
        insertarRecompensas();
        insertarDesafios();
        insertarNotificaciones();
        insertarEventos();
        insertPuntosReclamados();

        insertarRoles();
        insertarUsuarios();

        logger.info("Datos mock insertados correctamente");

    }

    private void insertarHogares() {

        logger.info("Insertando hogares, sectores y medidores...");

        int medidorId = 1;
        Random random = new Random();

        for (int hogarId = 1; hogarId <= cantidadHogares; hogarId++) {

            int cantidadMiembros = random.nextInt(5) + 1;
            int cantidadSectores = random.nextInt(3) + 1;

            //Insertar facturacion
            jdbcTemplate.update("INSERT INTO facturacion (plan_id, medio_de_pago) VALUES (?, ?);",
                    1, "TARJETA_CREDITO");

            jdbcTemplate.update("INSERT INTO Hogar (miembros, localidad, direccion, ambientes, tiene_patio, tiene_pileta, tipo_hogar, facturacion_id, email, racha_diaria, puntos_disponibles, nombre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    cantidadMiembros, randomLocalidad(), "Medrano 191", 2, true, false, "CASA", hogarId, "hogar" + hogarId + "@example.com", 0, 10, "MI HOGAR");

            //Insertar logros y medallas
            jdbcTemplate.update("INSERT INTO hogar_medallas (hogar_id, medalla_id) VALUES (?, ?);",
                    hogarId, 1);
            jdbcTemplate.update("INSERT INTO hogar_logros (hogar_id, logro_id) VALUES (?, ?);",
                    hogarId, 1);

            List<String> categorias = getCategorias(cantidadSectores);
            for (int j = 0; j < cantidadSectores; j++) {

                String categoria = categorias.get(j);

                //Insert medidor
                int numeroSerie;
                if (hogarId == 5 && j == 0) {
                    numeroSerie = 11000780; // fijo para el primer medidor del primer hogar
                } else {
                    numeroSerie = 100000 + hogarId * 1000 + j;
                }

                jdbcTemplate.update("INSERT INTO medidores (numero_serie, estado) VALUES (?, ?)",
                        numeroSerie, EstadoMedidor.ON.name());

                //Insert sector
                jdbcTemplate.update("INSERT INTO Sector (nombre, categoria_sector, hogar_id, medidor_id, umbral_mensual) VALUES (?, ?, ?, ?, ?)",
                        categoria, categoria, hogarId, medidorId, 10000);

                medidorId++;
            }

        }

    }

    private void insertarMediciones() {

        logger.info("Insertando mediciones...");

        Random random = new Random();

        List<Long> sectorIds = jdbcTemplate.query("SELECT id FROM Sector",
                (rs, rowNum) -> rs.getLong("id"));

        int totalMediciones = 15000;
        int batchSize = 5000;
        List<Object[]> batch = new ArrayList<>(batchSize);

        for (int i = 1; i <= totalMediciones; i++) {
            int flow = random.nextInt(100);
            LocalDateTime time = LocalDateTime.now().plusMonths(2); //Genera pasadas y futuras
            Timestamp ts = Timestamp.valueOf(time.minusMinutes(random.nextInt(150000)));
            //LocalDateTime.now().minusMinutes(random.nextInt(100000)));
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

    private void insertarRecompensas() {

        logger.info("Insertando recompensas...");

        List<Map<String, Object>> recompensas = List.of(
                Map.of("descripcion", "Descuento del 10% en medidor", "puntos", 50),
                Map.of("descripcion", "Descuento del 20% en medidor", "puntos", 90),
                Map.of("descripcion", "Descuento del 5% en mantenimiento", "puntos", 300)
        );

        for (Map<String, Object> recompensa : recompensas) {
            String descripcion = (String) recompensa.get("descripcion");
            int puntos = (int) recompensa.get("puntos");
            jdbcTemplate.update(
                    "INSERT INTO Recompensa (descripcion, puntos_necesarios) VALUES (?, ?)",
                    descripcion, puntos);
        }

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar",
                (rs, rowNum) -> rs.getLong("id"));

        LocalDateTime hoy = LocalDateTime.now();

        for (Long hogarId : hogaresIDs) {
            jdbcTemplate.update(
                    "INSERT INTO recompensa_hogar (hogar_id, recompensa_id, estado, fecha_de_reclamo) VALUES (?, ?, ?, ?)",
                    hogarId, 1, "DISPONIBLE", hoy);
        }

    }

    private void insertarDesafios(){
        logger.info("Insertando desafíos...");

        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Desafío Semanal", "Reduce tu consumo en un 10% esta semana", 100);
        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Inicio de sesion", "inicia sesion todos los dias de la semana", 200);
        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Juega un minijuego", "Jugar un minijuego de AquaQuest", 50);

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar",
                (rs, rowNum) -> rs.getLong("id"));

        LocalDateTime hoy = LocalDateTime.now();

        for (Long hogarId : hogaresIDs) {
            jdbcTemplate.update(
                    "INSERT INTO desafio_hogar (hogar_id, desafio_id, progreso) VALUES (?, ?, ?)",
                    hogarId, 1, 50);
        }
    }


  private void insertarLogrosYMedallas() {
    logger.info("Insertando logros y medallas...");

    jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Registro", "Te registraste en AquaVision");

    jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Hogar sustentable", "Redujiste el consumo usando AquaVision");

  }

  private void insertarPlanes(){
      logger.info("Insertando planes...");

      jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "BASICO", 2000.0);
      jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "PREMIUM", 5000.0);
      jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "FULL", 7000.0);



  }

    private void insertarNotificaciones() {
        logger.info("Insertando notificaciones...");

        List<Long> hogaresIDs = jdbcTemplate.query(
            "SELECT id FROM Hogar",
            (rs, rowNum) -> rs.getLong("id")
        );

        LocalDateTime hoy = LocalDateTime.now();

        for (Long hogarId : hogaresIDs) {
            TipoNotificacion tipo = TipoNotificacion.INFORME;
            String titulo = "📊💧 Informe de Consumo Mensual 💧📊";
            String mensaje = "Su consumo mensual de Noviembre ha sido de 5000 litros";
          jdbcTemplate.update(
              "INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
              hogarId, mensaje, hoy, titulo, false, tipo.name()
          );

          tipo = TipoNotificacion.ALERTA;
          titulo = "🚨 Alerta de sensor inactivo 🚨";
          mensaje = "Se ha detectado que uno de sus sensores se desconectó el dia: " + hoy.minusDays(2).toLocalDate();
          jdbcTemplate.update(
                  "INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                  hogarId, mensaje, hoy, titulo, false, tipo.name()
          );

          tipo = TipoNotificacion.ALERTA;
          titulo = "🚨💧 Alerta de fuga de agua detectada 💧🚨";
          mensaje = "Se ha detectado una posible pérdidad en el sector BAÑO el dia: " + hoy.minusDays(2).toLocalDate();
          jdbcTemplate.update(
                  "INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                  hogarId, mensaje, hoy, titulo, false, tipo.name()
          );
        }
    }

  private void insertarEventos(){

    logger.info("Insertando eventos...");

    List<Long> sectoresIDs = jdbcTemplate.query("SELECT id FROM Sector",
              (rs, rowNum) -> rs.getLong("id"));

    LocalDateTime hoy = LocalDateTime.now();

    for (Long sectorId : sectoresIDs) {
        jdbcTemplate.update(
            "INSERT INTO aqua_evento (costo, litros_consumidos, fecha_inicio, fecha_fin, sector_id, descripcion, titulo, estado_evento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                200, 3, hoy, hoy.plusMinutes(15), sectorId, "Evento de limpieza", "Limpieza", "FINALIZADO");
        jdbcTemplate.update(
                "INSERT INTO evento_tags (evento_id, tag_id) VALUES (?, ?)",
                sectorId, 1);
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


  private void insertPuntosReclamados() {
    logger.info("Insertando puntos reclamados...");

      List<Long> hogaresIDs = jdbcTemplate.query(
              "SELECT id FROM Hogar",
              (rs, rowNum) -> rs.getLong("id")
      );

      LocalDateTime fecha = LocalDateTime.now().minusDays(7); //Un par de dias de base
      String escena = null;

      for (Long hogarId : hogaresIDs) {
          String minijuego = Minijuego.AQUA_CARDS.name();
          int puntos = 15;
          jdbcTemplate.update("INSERT INTO puntos_reclamados (fecha, mini_juego, escena, puntos, hogar_id) VALUES (?, ?, ?, ?,?)", fecha, minijuego, null, puntos, hogarId);
      }
  }


  private void insertarUsuarios() {

    logger.info("Insertando usuarios...");

    List<User> usuarios = new ArrayList<>();
    usuarios.add(new User("matif", "test123", "Matias", "Fernandez", false));
    usuarios.add(new User("matip", "test123", "Matias", "Planchuelo", false));
    usuarios.add(new User("erik", "test123", "Erik", "Quispe", false));
    usuarios.add(new User("juan", "test123", "Juan", "Iwassjuk", false));
    usuarios.add(new User("agus", "test123", "Agustin", "Evans", false));
    usuarios.add(new User("aquavision", "test123", "Aqua", "Vision", false));
    usuarios.add(new User("aquavision", "test123", "Aqua", "Vision", true));

    for (int i = 1; usuarios.size() - 1 < cantidadHogares; i++) {
      usuarios.add(new User("user" + i, "test123", "User" + i, "User" + i, false));
    }

    Long roleUserId = jdbcTemplate.queryForObject("SELECT id FROM Role_ WHERE name = 'ROLE_USER'", Long.class);
    Long roleAdminId = jdbcTemplate.queryForObject("SELECT id FROM Role_ WHERE name = 'ROLE_ADMIN'", Long.class);

    List<Long> hogaresDisponibles = jdbcTemplate.query("SELECT id FROM Hogar",
        (rs, rowNum) -> rs.getLong("id"));

    if (hogaresDisponibles.size() < usuarios.size() - 1) {
      throw new IllegalStateException("No hay suficientes hogares para asignar a todos los usuarios");
    }

    int index = 0;
    for (User usuario : usuarios) {

      Long hogarId = hogaresDisponibles.get(index);

      Integer existe = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM User_ WHERE username = ?", Integer.class, usuario.getUsername());
      if (existe > 0) continue;

      if (usuario.isAdmin()) {
        hogarId = null; // El admin no tiene hogar asignado
      }

      jdbcTemplate.update("INSERT INTO User_ (username, password, name, surname, enabled, hogar_id) VALUES (?, ?, ?, ?, ?, ?)",
          usuario.getUsername(), passwordEncoder.encode(usuario.getPassword()), usuario.getName(), usuario.getSurname(), true, hogarId);

      Long userId = jdbcTemplate.queryForObject("SELECT id FROM User_ WHERE username = ?", Long.class, usuario.getUsername());
      jdbcTemplate.update("INSERT INTO Usuarios_Roles (user_id, role_id) VALUES (?, ?)",
          userId, usuario.isAdmin() ? roleAdminId : roleUserId);

      index++;
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