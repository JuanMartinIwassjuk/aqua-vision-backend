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
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private final int CANTIDAD_HOGARES = 7;

    private final List<String> NOMBRE_HOGARES = List.of(
            "AquaVision Team",
            "Hogar de Erik Quispe",
            "Hogar de Matias Planchuelo",
            "Hogar de Matias Fernandez",
            "Hogar de Juan Iwassjuk",
            "Hogar de Agustin Evans",
            "AquaVision Admin"
    );

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

        for (int hogarId = 1; hogarId <= CANTIDAD_HOGARES; hogarId++) {

            int cantidadMiembros = random.nextInt(5) + 1;
            int cantidadSectores = 3; //Todos tiene 3 sectores

            //Insertar facturacion
            jdbcTemplate.update("INSERT INTO facturacion (plan_id, medio_de_pago) VALUES (?, ?);",
                    1, "TARJETA_CREDITO");

            jdbcTemplate.update("INSERT INTO Hogar (miembros, localidad, direccion, ambientes, tiene_patio, tiene_pileta, tipo_hogar, facturacion_id, email, racha_diaria, puntos_disponibles, nombre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    cantidadMiembros, randomLocalidad(), "Medrano 191", 2, true, false, "CASA", hogarId, "hogar" + hogarId + "@example.com", 0, this.randomPuntos(), NOMBRE_HOGARES.get(hogarId - 1));

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

        logger.info("Iniciando la inserción de mediciones...");

        Random random = new Random();

        List<Long> sectorIds = jdbcTemplate.query("SELECT id FROM Sector", (rs, rowNum) -> rs.getLong("id"));

        // --- Rango de Fechas  ---
        LocalDateTime startTime = LocalDateTime.of(2025, 9, 1, 0, 0); // 1 de Septiembre 00:00
        LocalDateTime endTime = LocalDateTime.now();
        //LocalDateTime endTime = LocalDateTime.of(2025, 11, 26, 19, 0); // 26 de Noviembre 19:00

        long minutesInterval = 60; // CAMBIO CLAVE: Frecuencia de medición: cada 60 minutos (1 hora)

        int batchSize = 5000;
        List<Object[]> batch = new ArrayList<>(batchSize);

        LocalDateTime currentMeasurementTime = startTime;
        int totalMedicionesInsertadas = 0;

        // 2. Bucle principal que itera sobre cada intervalo de 60 minutos
        while (currentMeasurementTime.isBefore(endTime) || currentMeasurementTime.isEqual(endTime)) {

            // 3. Para cada intervalo, generar una medición para CADA sector
            for (Long sectorId : sectorIds) {

                Timestamp ts = Timestamp.valueOf(currentMeasurementTime);

                // Generar Caudal (flow) con lógica realista y nueva escala
                int flow = generateRealisticFlow(currentMeasurementTime, random);

                batch.add(new Object[]{flow, ts, sectorId});
                totalMedicionesInsertadas++;

                // 4. Inserción por lotes
                if (totalMedicionesInsertadas % batchSize == 0) {
                    jdbcTemplate.batchUpdate("INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
                    batch.clear();
                    logger.info("Insertadas {} mediciones (lote)", totalMedicionesInsertadas);
                }
            }

            // Avanzar al siguiente intervalo de tiempo
            currentMeasurementTime = currentMeasurementTime.plusMinutes(minutesInterval);
        }

        // 5. Inserción final del lote restante
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate("INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
        }

        logger.info("Inserción finalizada. Total de {} mediciones insertadas.", totalMedicionesInsertadas);
    }

    private void insertarRecompensas() {

        logger.info("Insertando recompensas...");

        List<Map<String, Object>> recompensas = List.of(
                Map.of("descripcion", "Descuento del 10% en medidor", "puntos", 1000),
                Map.of("descripcion", "Descuento del 20% en medidor", "puntos", 1200),
                Map.of("descripcion", "Descuento del 5% en mantenimiento", "puntos", 2000),
                Map.of("descripcion", "Descuento del 15% en mantenimiento", "puntos", 2300),
                Map.of("descripcion", "Descuento del 25% en plan premium anual", "puntos", 5000)
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
            jdbcTemplate.update(
                    "INSERT INTO desafio_hogar (hogar_id, desafio_id, progreso) VALUES (?, ?, ?)",
                    hogarId, 2, 75);
            jdbcTemplate.update(
                    "INSERT INTO desafio_hogar (hogar_id, desafio_id, progreso) VALUES (?, ?, ?)",
                    hogarId, 3, 100);
        }
    }

  private void insertarLogrosYMedallas() {
    logger.info("Insertando logros y medallas...");

    //LOGROS
    jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Registro", "Te registraste en AquaVision");
    jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Medidor", "Conectase un medidor en tu hogar");
    jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Top Ranking", "Lograste llegar al top de hogares");

    //MEDALLAS
    jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Hogar sustentable", "Redujiste el consumo usando AquaVision");
    jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Aqua Expert", "Completaste todos los desafíos");
    jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Eco Warrior", "Has alcanzado 10000 puntos en AquaVision");

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

    LocalDateTime hoy = LocalDateTime.now().minusHours(5);
    Long eventoId = 1L;

    for (Long sectorId : sectoresIDs) {
        jdbcTemplate.update(
            "INSERT INTO aqua_evento (costo, litros_consumidos, fecha_inicio, fecha_fin, sector_id, descripcion, titulo, estado_evento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                200, 3, hoy, hoy.plusHours(1), sectorId, "Evento de limpieza", "Limpieza", "FINALIZADO");
        jdbcTemplate.update(
                "INSERT INTO evento_tags (evento_id, tag_id) VALUES (?, ?)",
                eventoId, 1);
        eventoId++;
        jdbcTemplate.update(
                "INSERT INTO aqua_evento (costo, litros_consumidos, fecha_inicio, fecha_fin, sector_id, descripcion, titulo, estado_evento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                300, 15, hoy.plusHours(2), hoy.plusHours(3), sectorId, "Evento de Lavado", "Lavado", "FINALIZADO");
        jdbcTemplate.update(
                "INSERT INTO evento_tags (evento_id, tag_id) VALUES (?, ?)",
                eventoId, 8);
        eventoId++;
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
    usuarios.add(new User("aquavisiondemo", "test123", "AquaVision", "Team", false));
    usuarios.add(new User("erik", "test123", "Erik", "Quispe", false));
    usuarios.add(new User("matip", "test123", "Matias", "Planchuelo", false));
    usuarios.add(new User("matif", "test123", "Matias", "Fernandez", false));
    usuarios.add(new User("juan", "test123", "Juan", "Iwassjuk", false));
    usuarios.add(new User("agus", "test123", "Agustin", "Evans", false));
    usuarios.add(new User("aquavision", "test123", "AquaVision", "Admin", true));

    for (int i = 1; usuarios.size() < CANTIDAD_HOGARES; i++) {
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

  private int randomPuntos() {
    return new Random().nextInt(1000);
  }

  private boolean datosMockInsertados() {
    Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Hogar", Integer.class);
    return count != null && count > 0;
  }

    /**
     * Genera un valor de caudal (flow) realista BASADO EN 300 LITROS/DIA POR HOGAR.
     * El valor de retorno representa los LITROS CONSUMIDOS POR SECTOR en la hora de medición.
     */
    private int generateRealisticFlow(LocalDateTime time, Random random) {
        int hour = time.getHour();
        DayOfWeek day = time.getDayOfWeek();

        // Se define el rango base de consumo [min, max] para la hora actual (en LITROS/HORA por SECTOR)
        int minFlow;
        int maxFlow;

        // --- Definición de rangos para Días de Semana (Lunes a Viernes) ---
        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
            if (hour >= 6 && hour <= 8) { // Pico Mañana: Uso de BAÑO (ducha, inodoro)
                minFlow = 5; maxFlow = 15;
            } else if (hour >= 12 && hour <= 13) { // Pico Mediodía: Uso de COCINA
                minFlow = 2; maxFlow = 8;
            } else if (hour >= 19 && hour <= 21) { // Pico Noche: Uso de COCINA/LAVADERO (platos, ropa)
                minFlow = 8; maxFlow = 20;
            } else if (hour >= 9 && hour <= 18) { // Horas Intermedias
                minFlow = 0; maxFlow = 3;
            } else { // Noche Profunda/Madrugada
                minFlow = 0; maxFlow = 1; // Mínimo o nulo
            }
        }
        // --- Definición de rangos para Fin de Semana (Sábado y Domingo) ---
        else {
            if (hour >= 8 && hour <= 10) { // Pico Mañana
                minFlow = 4; maxFlow = 12;
            } else if (hour >= 14 && hour <= 16) { // Pico Tarde/Almuerzo
                minFlow = 5; maxFlow = 15;
            } else if (hour >= 19 && hour <= 22) { // Pico Noche (Máximo consumo del fin de semana)
                minFlow = 10; maxFlow = 25;
            } else if (hour >= 11 && hour <= 13 || hour >= 17 && hour <= 18) { // Horas Intermedias
                minFlow = 3; maxFlow = 10;
            } else { // Noche Profunda/Madrugada
                minFlow = 0; maxFlow = 1;
            }
        }

        // ... (Lógica de ruido y limitación)
        if (minFlow > maxFlow) {
            maxFlow = minFlow;
        }

        int baseFlow = random.nextInt(maxFlow - minFlow + 1) + minFlow;
        int noise = random.nextInt(3) - 1; // Ruido: [-1, 0, 1]

        // Limitar el resultado final (Máximo de 30 litros/hora por sector)
        return Math.max(0, Math.min(30, baseFlow + noise));
    }
}

