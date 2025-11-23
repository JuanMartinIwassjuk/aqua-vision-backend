package com.app.aquavision.boostrap;

import com.app.aquavision.entities.User;
import com.app.aquavision.entities.domain.EstadoMedidor;

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
import java.util.Collections;
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

    private final Random random = new Random();


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

        insertarRecompensas(); // crea recompensas globales
        asignarRecompensasHogarAleatorias(); // asigna estados variados por hogar

        insertarDesafios(); // crea desafios base
        asignarDesafiosHogarRandom(); // actualiza progreso y reclamado por hogar

        insertarNotificaciones();
        insertarEventos();

        insertarPuntosReclamadosEnhanced(); // reemplaza al insertPuntosReclamados simple

        insertarRoles();
        insertarUsuarios();

        logger.info("Datos mock insertados correctamente");

    }

    private void insertarPlanes(){ logger.info("Insertando planes..."); jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "BASICO", 2000.0); jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "PREMIUM", 5000.0); jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "FULL", 7000.0); }

    /**
     * Inserta hogares + sectores + medidores.
     * Modificado para asignar rachaDiaria y puntosDisponibles coherentes.
     */
    private void insertarHogares() {

        logger.info("Insertando hogares, sectores y medidores...");

        int medidorId = 1;

        for (int hogarId = 1; hogarId <= CANTIDAD_HOGARES; hogarId++) {

            int cantidadMiembros = random.nextInt(5) + 1;
            int cantidadSectores = 3; //Todos tiene 3 sectores

            //Insertar facturacion
            jdbcTemplate.update("INSERT INTO facturacion (plan_id, medio_de_pago) VALUES (?, ?);",
                    1, "TARJETA_CREDITO");

            // Generar racha diaria: 0..14 días, con probabilidad de rachas más cortas
            int rachaDiaria = generarRachaPonderada();

            // Generar puntos disponibles: basado en racha y logros (por ejemplo)
            int puntosDisponibles = generarPuntosIniciales(rachaDiaria);

            jdbcTemplate.update("INSERT INTO Hogar (miembros, localidad, direccion, ambientes, tiene_patio, tiene_pileta, tipo_hogar, facturacion_id, email, racha_diaria, puntos_disponibles, nombre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    cantidadMiembros, randomLocalidad(), "Medrano 191", 2, random.nextBoolean(), random.nextBoolean(), "CASA", hogarId, "hogar" + hogarId + "@example.com", rachaDiaria, puntosDisponibles, NOMBRE_HOGARES.get(hogarId - 1));

            // no insertar aquí medallas/logros fijos; lo hacemos después de crear todos los hogares

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

        // Luego de insertar hogares, asignar medallas y logros aleatorios por hogar
        asignarMedallasYLogrosRandomly();
    }

    private int generarRachaPonderada() {
        // rachas cortas son más probables, rachas largas menos.
        int roll = random.nextInt(100);
        if (roll < 40) return random.nextInt(3);            // 0-2 (40%)
        if (roll < 75) return random.nextInt(7);            // 0-6 (35%)
        if (roll < 95) return 7 + random.nextInt(8);        // 7-14 (20%)
        return 15 + random.nextInt(16);                     // 15-30 (5%)
    }

    private int generarPuntosIniciales(int rachaDiaria) {
        // Base: 0..500 + racha*20 + bonus aleatorio por logros potenciales
        int base = random.nextInt(600);
        int bonusRacha = rachaDiaria * (10 + random.nextInt(21)); // 10..30 por día
        int extra = random.nextInt(500);
        return Math.max(0, base + bonusRacha + extra);
    }

    private void insertarMediciones() {

        logger.info("Iniciando la inserción de mediciones...");

        List<Long> sectorIds = jdbcTemplate.query("SELECT id FROM Sector", (rs, rowNum) -> rs.getLong("id"));

        // --- Rango de Fechas  ---
        LocalDateTime startTime = LocalDateTime.of(2025, 9, 1, 0, 0); // 1 de Septiembre 00:00
        LocalDateTime endTime = LocalDateTime.now();

        long minutesInterval = 60; // Frecuencia de medición: cada 60 minutos

        int batchSize = 5000;
        List<Object[]> batch = new ArrayList<>(batchSize);

        LocalDateTime currentMeasurementTime = startTime;
        int totalMedicionesInsertadas = 0;

        while (currentMeasurementTime.isBefore(endTime) || currentMeasurementTime.isEqual(endTime)) {

            for (Long sectorId : sectorIds) {

                Timestamp ts = Timestamp.valueOf(currentMeasurementTime);
                int flow = generateRealisticFlow(currentMeasurementTime, random);

                batch.add(new Object[]{flow, ts, sectorId});
                totalMedicionesInsertadas++;

                if (totalMedicionesInsertadas % batchSize == 0) {
                    jdbcTemplate.batchUpdate("INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
                    batch.clear();
                    logger.info("Insertadas {} mediciones (lote)", totalMedicionesInsertadas);
                }
            }

            currentMeasurementTime = currentMeasurementTime.plusMinutes(minutesInterval);
        }

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
    }

    /**
     * Asigna una recompensa por hogar con estados variados: DISPONIBLE, CANJEADA, EXPIRADA.
     * También distribuye fechas de reclamo acordes (si CANJEADA o EXPIRADA).
     */
    private void asignarRecompensasHogarAleatorias() {
        logger.info("Asignando recompensas a hogares con estados variados...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar",
                (rs, rowNum) -> rs.getLong("id"));

        // Obtener reward ids
        List<Long> recompensaIds = jdbcTemplate.query("SELECT id FROM Recompensa",
                (rs, rowNum) -> rs.getLong("id"));

        LocalDateTime now = LocalDateTime.now();

        for (Long hogarId : hogaresIDs) {
            // cada hogar obtiene 1 o 2 recompensas asignadas
            int cantidad = 1 + random.nextInt(2);
            for (int i = 0; i < cantidad; i++) {
                Long recompensaId = recompensaIds.get(random.nextInt(recompensaIds.size()));

                // probabilidades: DISPONIBLE 60%, CANJEADA 25%, EXPIRADA 15%
                int roll = random.nextInt(100);
                String estado;
                LocalDateTime fecha = null;
                if (roll < 60) {
                    estado = "DISPONIBLE";
                    fecha = now.minusDays(random.nextInt(5)); // recientemente disponible
                } else if (roll < 85) {
                    estado = "CANJEADA";
                    fecha = now.minusDays(5 + random.nextInt(30));
                } else {
                    estado = "EXPIRADA";
                    fecha = now.minusDays(31 + random.nextInt(60));
                }

                jdbcTemplate.update(
                        "INSERT INTO recompensa_hogar (hogar_id, recompensa_id, estado, fecha_de_reclamo) VALUES (?, ?, ?, ?)",
                        hogarId, recompensaId, estado, fecha
                );
            }
        }
    }

    private void insertarDesafios(){
        logger.info("Insertando desafíos...");

        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Desafío Semanal", "Reduce tu consumo en un 10% esta semana", 100);
        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Inicio de sesion", "Inicia sesion todos los dias de la semana", 200);
        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Juega un minijuego", "Jugar un minijuego de AquaQuest", 50);
    }

    /**
     * Asigna progreso aleatorio/creíble a los desafio_hogar existentes o crea entradas si no existen.
     */
    private void asignarDesafiosHogarRandom() {
        logger.info("Asignando progreso de desafíos por hogar...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs, rowNum) -> rs.getLong("id"));
        List<Long> desafioIds = jdbcTemplate.query("SELECT id FROM desafio", (rs, rowNum) -> rs.getLong("id"));

        for (Long hogarId : hogaresIDs) {
            for (Long desafioId : desafioIds) {
                int progreso = generarProgresoPorTipoDesafio(desafioId);
                boolean reclamado = false;
                // si progreso==100 tener una probabilidad de haber reclamado
                if (progreso >= 100) {
                    reclamado = random.nextInt(100) < 60; // 60% chance reclamado
                } else {
                    reclamado = random.nextInt(100) < 5; // reclamado erroneamente raro
                }

                // Insert or update: para simplificar, intentar insertar
                jdbcTemplate.update(
                        "INSERT INTO desafio_hogar (hogar_id, desafio_id, progreso, reclamado) VALUES (?, ?, ?, ?)",
                        hogarId, desafioId, Math.min(100, progreso), reclamado ? 1 : 0
                );
            }
        }
    }

    private int generarProgresoPorTipoDesafio(Long desafioId) {
        // Puedes mapear por id si conoces los ids fijos; sino generar aleatorio coherente
        int roll = random.nextInt(100);
        if (roll < 30) return random.nextInt(50);         // 0-49
        if (roll < 70) return 50 + random.nextInt(30);    // 50-79
        return 80 + random.nextInt(21);                   // 80-100
    }

    private void insertarLogrosYMedallas() {
        logger.info("Insertando logros y medallas...");

        //LOGROS
        jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Registro", "Te registraste en AquaVision");
        jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Medidor", "Conectaste un medidor en tu hogar");
        jdbcTemplate.update("INSERT INTO Logro (nombre, descripcion) VALUES (?, ?)", "Top Ranking", "Lograste llegar al top de hogares");

        //MEDALLAS
        jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Hogar sustentable", "Redujiste el consumo usando AquaVision");
        jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Aqua Expert", "Completaste todos los desafíos");
        jdbcTemplate.update("INSERT INTO Medalla (nombre, descripcion) VALUES (?, ?)", "Eco Warrior", "Has alcanzado 10000 puntos en AquaVision");
    }

    /**
     * Asigna aleatoriamente 0..3 medallas y 0..3 logros por hogar (evitando duplicados).
     */
    private void asignarMedallasYLogrosRandomly() {
        logger.info("Asignando medallas y logros aleatoriamente a hogares...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs, rowNum) -> rs.getLong("id"));
        List<Long> medallaIds = jdbcTemplate.query("SELECT id FROM Medalla", (rs, rowNum) -> rs.getLong("id"));
        List<Long> logroIds = jdbcTemplate.query("SELECT id FROM Logro", (rs, rowNum) -> rs.getLong("id"));

        for (Long hogarId : hogaresIDs) {
            // medallas
            int medallasCount = random.nextInt(Math.min(3, medallaIds.size()) + 1); // 0..3
            Collections.shuffle(medallaIds, random);
            for (int i = 0; i < medallasCount; i++) {
                jdbcTemplate.update("INSERT INTO hogar_medallas (hogar_id, medalla_id) VALUES (?, ?);", hogarId, medallaIds.get(i));
            }

            // logros
            int logrosCount = random.nextInt(Math.min(3, logroIds.size()) + 1); // 0..3
            Collections.shuffle(logroIds, random);
            for (int i = 0; i < logrosCount; i++) {
                jdbcTemplate.update("INSERT INTO hogar_logros (hogar_id, logro_id) VALUES (?, ?);", hogarId, logroIds.get(i));
            }
        }
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
            mensaje = "Se ha detectado una posible pérdida en el sector BAÑO el dia: " + hoy.minusDays(2).toLocalDate();
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

/**
 * Inserta puntos reclamados asegurándose de usar un valor válido para la columna mini_juego.
 * Extrae los valores permitidos desde INFORMATION_SCHEMA para evitar "Data truncated" por ENUMs.
 */
private void insertarPuntosReclamadosEnhanced() {
    logger.info("Insertando puntos reclamados (mejorado y variado)...");

    List<Long> hogaresIDs = jdbcTemplate.query(
            "SELECT id FROM Hogar",
            (rs, rowNum) -> rs.getLong("id")
    );

    // Obtener valores permitidos para la columna mini_juego (si es ENUM)
    List<String> miniJuegoAllowed = getAllowedEnumValues("puntos_reclamados", "mini_juego");
    if (miniJuegoAllowed.isEmpty()) {
        // fallback razonable si por alguna razón no conseguimos los valores
        miniJuegoAllowed = List.of("AQUA_CARDS");
    }

    for (Long hogarId : hogaresIDs) {
        int cantidad = 1 + random.nextInt(6); // 1..6
        for (int i = 0; i < cantidad; i++) {
            LocalDateTime fecha = LocalDateTime.now()
                    .minusDays(random.nextInt(45))
                    .withHour(random.nextInt(24))
                    .withMinute(random.nextInt(60))
                    .withSecond(random.nextInt(60));

            int puntos = generarPuntosReclamadosValor();

            // elegir un minijuego válido de entre los permitidos por la DB
            String minijuego = miniJuegoAllowed.get(random.nextInt(miniJuegoAllowed.size()));

            String escena = generarEscenaAleatoria();

            jdbcTemplate.update(
                    "INSERT INTO puntos_reclamados (fecha, mini_juego, escena, puntos, hogar_id) VALUES (?, ?, ?, ?, ?)",
                    fecha, minijuego, escena, puntos, hogarId);

            // actualizar puntos disponibles en Hogar de forma coherente
            jdbcTemplate.update("UPDATE Hogar SET puntos_disponibles = puntos_disponibles + ? WHERE id = ?", puntos, hogarId);
        }
    }
}

/**
 * Devuelve la lista de valores permitidos para una columna ENUM (por ejemplo: "enum('A','B')").
 * Si la columna no existe o hay error, devuelve lista vacía.
 */
private List<String> getAllowedEnumValues(String tableName, String columnName) {
    try {
        String columnType = jdbcTemplate.queryForObject(
                "SELECT COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                String.class,
                tableName,
                columnName
        );

        if (columnType == null) return Collections.emptyList();

        columnType = columnType.trim();
        // Esperamos algo como: enum('A','B','C')
        if (columnType.toLowerCase().startsWith("enum(") && columnType.endsWith(")")) {
            String inner = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
            // inner =  'A','B','C'
            // quitar comillas y split
            // split por ',' pero cuidado con comillas
            List<String> values = new ArrayList<>();
            // dividir considerando que cada valor va entre comillas simples y separado por ,
            // así hacemos un replace de ' y luego split por , pero respetando comillas simples originales:
            String[] parts = inner.split("','");
            for (int i = 0; i < parts.length; i++) {
                String p = parts[i];
                // limpiar comillas sobrantes al inicio/final
                p = p.replaceAll("^'+", "").replaceAll("'+$", "");
                values.add(p);
            }
            return values;
        } else {
            // si no es enum (por ejemplo varchar), podemos devolver vacío y el caller hará fallback
            return Collections.emptyList();
        }
    } catch (Exception ex) {
        logger.warn("No se pudieron obtener valores permitidos para {}.{} -> {}", tableName, columnName, ex.getMessage());
        return Collections.emptyList();
    }
}


    private int generarPuntosReclamadosValor() {
        int roll = random.nextInt(100);
        if (roll < 60) return 5 + random.nextInt(46);      // 5-50 (60%)
        if (roll < 90) return 50 + random.nextInt(151);    // 50-200 (30%)
        return 200 + random.nextInt(301);                  // 200-500 (10%)
    }

    private String generarEscenaAleatoria() {
        int r = random.nextInt(100);
        if (r < 40) return null;
        if (r < 70) return "MENU_PRINCIPAL";
        if (r < 85) return "MINIJUEGO_RESULTADOS";
        return "BONUS_ROUND";
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

        int minFlow;
        int maxFlow;

        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
            if (hour >= 6 && hour <= 8) {
                minFlow = 5; maxFlow = 15;
            } else if (hour >= 12 && hour <= 13) {
                minFlow = 2; maxFlow = 8;
            } else if (hour >= 19 && hour <= 21) {
                minFlow = 8; maxFlow = 20;
            } else if (hour >= 9 && hour <= 18) {
                minFlow = 0; maxFlow = 3;
            } else {
                minFlow = 0; maxFlow = 1;
            }
        } else {
            if (hour >= 8 && hour <= 10) {
                minFlow = 4; maxFlow = 12;
            } else if (hour >= 14 && hour <= 16) {
                minFlow = 5; maxFlow = 15;
            } else if (hour >= 19 && hour <= 22) {
                minFlow = 10; maxFlow = 25;
            } else if (hour >= 11 && hour <= 13 || hour >= 17 && hour <= 18) {
                minFlow = 3; maxFlow = 10;
            } else {
                minFlow = 0; maxFlow = 1;
            }
        }

        if (minFlow > maxFlow) {
            maxFlow = minFlow;
        }

        int baseFlow = random.nextInt(maxFlow - minFlow + 1) + minFlow;
        int noise = random.nextInt(3) - 1; // Ruido: [-1, 0, 1]

        return Math.max(0, Math.min(30, baseFlow + noise));
    }
}
