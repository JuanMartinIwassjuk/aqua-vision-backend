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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

    // localidades fijas para asegurar 4-5 localidades con datos
    private final List<String> LOCALIDADES = List.of("Palermo", "Belgrano", "Recoleta", "Caballito", "Flores");

    private static final Logger logger = LoggerFactory.getLogger(DataMock.class);

    private final Random random = new Random();

    private final List<String> MINIJUEGOS_POSSIBLES = List.of("AQUA_CARDS", "MINI_QUIZ", "RUEDA_FORTUNA");

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

        insertarRoles();
        insertarUsuarios();

        insertarMediciones(); // inserta histórico hasta ahora (sin admin)

        insertarRecompensas();
        asignarRecompensasHogarAleatorias();

        insertarDesafios();
        asignarDesafiosHogarRandom();

        insertarNotificacionesBaseline(); // ahora no inserta consumo mensual registrado

        insertarEventosHistoricos6Meses(); // eventos de los últimos 6 meses con tags
        //insertarPuntosReclamados6Meses(); // puntos reclamados distribuidos 6 meses

        // Datos especiales para el día actual (demo):
        insertarDatosEspecialDiaActual();


        logger.info("Datos mock insertados correctamente");
    }

    /**************
     * PLANES
     **************/
    private void insertarPlanes(){
        logger.info("Insertando planes...");
        jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "BASICO", 2000.0);
        jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "PREMIUM", 5000.0);
        jdbcTemplate.update("INSERT INTO Plan (tipo_plan, costo_mensual) VALUES (?, ?)", "FULL", 7000.0);
    }

    /**************
     * HOGARES + SECTORES + MEDIDORES
     **************/
    private void insertarHogares() {

        logger.info("Insertando hogares, sectores y medidores...");

        int medidorId = 1;

        for (int hogarId = 1; hogarId <= CANTIDAD_HOGARES; hogarId++) {

            int cantidadMiembros = random.nextInt(5) + 1;
            int cantidadSectores = 3; // por defecto intentamos crear 3 sectores

            //Insertar facturacion
            jdbcTemplate.update("INSERT INTO facturacion (plan_id, medio_de_pago) VALUES (?, ?);",
                    1, "TARJETA_CREDITO");

            // Racha y puntos
            int rachaDiaria;
            int puntosDisponibles;

            // Si es el hogar admin (último en la lista), dejar vacío y sin racha ni puntos
            boolean esAdmin = (hogarId == CANTIDAD_HOGARES);

            if (esAdmin) {
                rachaDiaria = 0;
                puntosDisponibles = 0;
            } else {
                rachaDiaria = generarRachaPonderada();
                puntosDisponibles = generarPuntosIniciales(rachaDiaria);
            }

            // Asignar localidad en round-robin de LOCALIDADES para asegurar variedad
            String localidad = LOCALIDADES.get((hogarId - 1) % LOCALIDADES.size());

            jdbcTemplate.update("INSERT INTO Hogar (miembros, localidad, direccion, ambientes, tiene_patio, tiene_pileta, tipo_hogar, facturacion_id, email, racha_diaria, puntos_disponibles, nombre) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    cantidadMiembros, localidad, "Medrano 191", 2, (!esAdmin && random.nextBoolean()), (!esAdmin && random.nextBoolean()), "CASA", hogarId, "hogar" + hogarId + "@example.com", rachaDiaria, (puntosDisponibles / 10), NOMBRE_HOGARES.get(hogarId - 1));

            // Si es admin: NO crear sectores ni medidores (queda sin nada)
            if (esAdmin) {
                continue;
            }

            List<String> categorias = getCategorias(cantidadSectores);
            for (int j = 0; j < cantidadSectores; j++) {

                String categoria = categorias.get(j);

                int numeroSerie;
                if (hogarId == 5 && j == 0) {
                    numeroSerie = 11000780; // fijo para el primer medidor del hogar 5
                } else {
                    numeroSerie = 100000 + hogarId * 1000 + j;
                }

                jdbcTemplate.update("INSERT INTO medidores (numero_serie, estado) VALUES (?, ?)",
                        numeroSerie, EstadoMedidor.ON.name());

                jdbcTemplate.update("INSERT INTO Sector (nombre, categoria_sector, hogar_id, medidor_id, umbral_mensual) VALUES (?, ?, ?, ?, ?)",
                        categoria, categoria, hogarId, medidorId, 10000);

                medidorId++;
            }
        }

        asignarMedallasYLogrosRandomly();
    }

    private int generarRachaPonderada() {
        int roll = random.nextInt(100);
        if (roll < 40) return random.nextInt(3);            // 0-2 (40%)
        if (roll < 75) return random.nextInt(7);            // 0-6 (35%)
        if (roll < 95) return 7 + random.nextInt(8);        // 7-14 (20%)
        return 15 + random.nextInt(16);                     // 15-30 (5%)
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

    private int generarPuntosIniciales(int rachaDiaria) {
        int base = random.nextInt(600);
        int bonusRacha = rachaDiaria * (10 + random.nextInt(21));
        int extra = random.nextInt(500);
        return Math.max(0, base + bonusRacha + extra);
    }

    /**************
     * MEDICIONES HISTÓRICAS
     * (inserta mediciones horarias desde 2025-09-01 hasta ahora)
     * Notas: No inserta mediciones para el hogar "AquaVision Admin".
     **************/
    private void insertarMediciones() {

        logger.info("Iniciando la inserción de mediciones (histórico hasta hoy, excluyendo admin)...");

        // Obtener id del hogar admin para excluirlo
        Integer adminHomeId = null;
        try {
            adminHomeId = jdbcTemplate.queryForObject("SELECT id FROM Hogar WHERE nombre = ? LIMIT 1", Integer.class, "AquaVision Admin");
        } catch (Exception ex) {
            // ignore si no existe
        }

        // Obtener sectores excluyendo sector perteneciente al hogar admin (si existe)
        List<Long> sectorIds;
        if (adminHomeId != null) {
            sectorIds = jdbcTemplate.query("SELECT id FROM Sector WHERE hogar_id <> ?", (rs, rowNum) -> rs.getLong("id"), adminHomeId);
        } else {
            sectorIds = jdbcTemplate.query("SELECT id FROM Sector", (rs, rowNum) -> rs.getLong("id"));
        }

        LocalDateTime startTime = LocalDateTime.of(2025, 9, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.now();

        long minutesInterval = 120; // cada 2 horas
        int batchSize = 5000;
        List<Object[]> batch = new ArrayList<>(batchSize);

        LocalDateTime currentMeasurementTime = startTime;
        int totalMedicionesInsertadas = 0;

        while (currentMeasurementTime.isBefore(endTime) || currentMeasurementTime.isEqual(endTime)) {

            for (Long sectorId : sectorIds) {

                Timestamp ts = Timestamp.valueOf(currentMeasurementTime);
                int flow = generateRealisticFlow(currentMeasurementTime, sectorId, random);


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

    /**************
     * RECOMPENSAS
     **************/
    private void insertarRecompensas() {
        logger.info("Insertando recompensas...");

        List<Map<String, Object>> recompensas = List.of(
                Map.of("descripcion", "Descuento del 10% en medidor", "puntos", 1000),
                Map.of("descripcion", "Descuento del 20% en medidor", "puntos", 1200),
                Map.of("descripcion", "Descuento del 5% en mantenimiento", "puntos", 2000),
                Map.of("descripcion", "Descuento del 15% en mantenimiento", "puntos", 2300),
                Map.of("descripcion", "Descuento del 25% en plan premium anual", "puntos", 5000)
        );

        for (Map<String, Object> r : recompensas) {
            jdbcTemplate.update("INSERT INTO Recompensa (descripcion, puntos_necesarios) VALUES (?, ?)", r.get("descripcion"), r.get("puntos"));
        }
    }

    private void asignarRecompensasHogarAleatorias() {
        logger.info("Asignando recompensas a hogares con estados variados (fechas distribuidas 6 meses)...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs, rowNum) -> rs.getLong("id"));
        List<Long> recompensaIds = jdbcTemplate.query("SELECT id FROM Recompensa", (rs, rowNum) -> rs.getLong("id"));

        LocalDateTime now = LocalDateTime.now();
        for (Long hogarId : hogaresIDs) {
            int cantidad = 1 + random.nextInt(2);
            for (int i = 0; i < cantidad; i++) {
                Long recompensaId = recompensaIds.get(random.nextInt(recompensaIds.size()));
                int roll = random.nextInt(100);
                String estado;
                LocalDateTime fecha = null;
                if (roll < 60) {
                    estado = "DISPONIBLE";
                    fecha = now.minusDays(random.nextInt(30));
                } else if (roll < 85) {
                    estado = "CANJEADA";
                    fecha = now.minusDays(random.nextInt(180 - 30) + 30); // 1-6 meses atrás
                } else {
                    estado = "EXPIRADA";
                    fecha = now.minusDays(180 + random.nextInt(60));
                }

                jdbcTemplate.update("INSERT INTO recompensa_hogar (hogar_id, recompensa_id, estado, fecha_de_reclamo) VALUES (?, ?, ?, ?)",
                        hogarId, recompensaId, estado, fecha);
            }
        }
    }

    /**************
     * DESAFIOS
     **************/
    private void insertarDesafios(){
        logger.info("Insertando desafíos...");

        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Desafío Semanal", "Reduce tu consumo en un 10% esta semana", 100);
        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Inicio de sesion", "Inicia sesion todos los dias de la semana", 200);
        jdbcTemplate.update("INSERT INTO desafio (titulo, descripcion, puntos_recompensa) VALUES (?, ?, ?)",
                "Juega un minijuego", "Jugar un minijuego de AquaQuest", 50);
    }

    private void asignarDesafiosHogarRandom() {
        logger.info("Asignando progreso de desafíos por hogar...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs, rowNum) -> rs.getLong("id"));
        List<Long> desafioIds = jdbcTemplate.query("SELECT id FROM desafio", (rs, rowNum) -> rs.getLong("id"));

        for (Long hogarId : hogaresIDs) {
            for (Long desafioId : desafioIds) {
                int progreso = generarProgresoPorTipoDesafio(desafioId);
                boolean reclamado = (progreso >= 100) ? (random.nextInt(100) < 60) : (random.nextInt(100) < 5);
                jdbcTemplate.update("INSERT INTO desafio_hogar (hogar_id, desafio_id, progreso, reclamado) VALUES (?, ?, ?, ?)",
                        hogarId, desafioId, Math.min(100, progreso), reclamado ? 1 : 0);
            }
        }
    }

    private int generarProgresoPorTipoDesafio(Long desafioId) {
        int roll = random.nextInt(100);
        if (roll < 30) return random.nextInt(50);
        if (roll < 70) return 50 + random.nextInt(30);
        return 80 + random.nextInt(21);
    }

    /**************
     * NOTIFICACIONES BASELINE (ahora: bienvenida, sin 'consumo mensual registrado')
     **************/
    private void insertarNotificacionesBaseline() {
        logger.info("Insertando notificaciones baseline (sin consumo mensual registrado)...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs, rowNum) -> rs.getLong("id"));
        LocalDateTime hoy = LocalDateTime.now();

        for (Long hogarId : hogaresIDs) {
            // No insertamos la notificacion "consumo mensual registrado"
            // Insertamos una notificacion de bienvenida (mas neutra) para no llenar de mensajes irrelevantes
            String titulo = "👋 Bienvenido a AquaVision";
            String mensaje = "Te damos la bienvenida a AquaVision. Revisa tu panel para ver tu consumo y actividades.";
            jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                    hogarId, mensaje, hoy.minusDays(1), titulo, false, TipoNotificacion.INFORME.name());
        }
    }

    /**************
     * EVENTOS HISTÓRICOS (ULTIMOS 6 MESES) con tags variados
     **************/
    private void insertarEventosHistoricos6Meses() {
        logger.info("Insertando eventos históricos (últimos 6 meses) con tags...");

        List<Long> sectoresIDs = jdbcTemplate.query("SELECT id FROM Sector", (rs,rowNum) -> rs.getLong("id"));
        LocalDateTime now = LocalDateTime.now();
        long eventoId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id),0) FROM aqua_evento", Long.class) + 1;

        // tag ids a usar (ajustar si tus tags tienen otros ids)
        List<Long> tagPool = new ArrayList<>(List.of(1L, 2L, 3L, 4L));

        for (Long sectorId : sectoresIDs) {
            // por sector generar 2-6 eventos distribuidos en últimos 6 meses
            int n = 2 + random.nextInt(5);
            for (int i = 0; i < n; i++) {
                int diasAtras = random.nextInt(180); // 0..179 días = ~6 meses
                LocalDateTime inicio = now.minusDays(diasAtras).withHour(10 + random.nextInt(10)).withMinute(0).withSecond(0);
                LocalDateTime fin = inicio.plusHours(1 + random.nextInt(3));
                int litros = 5 + random.nextInt(200);
                double costo = Math.round((litros * 0.05) * 100.0) / 100.0;
                String titulo = (random.nextBoolean()) ? "Lavado" : "Limpieza";
                String desc = titulo + " de actividad";

                jdbcTemplate.update("INSERT INTO aqua_evento (costo, litros_consumidos, fecha_inicio, fecha_fin, sector_id, descripcion, titulo, estado_evento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        costo, litros, inicio, fin, sectorId, desc, titulo, "FINALIZADO");

                // asignar 1-2 tags aleatorios
                Collections.shuffle(tagPool, random);
                int tagsToAssign = 1 + random.nextInt(2);
                for (int t = 0; t < tagsToAssign; t++) {
                    jdbcTemplate.update("INSERT INTO evento_tags (evento_id, tag_id) VALUES (?, ?)", eventoId, tagPool.get(t));
                }
                eventoId++;
            }
        }
    }

    /**************
     * PUNTOS RECLAMADOS - DISTRIBUIDOS 6 MESES
     **************/
    private void insertarPuntosReclamados6Meses() {
        logger.info("Insertando puntos reclamados distribuidos en los últimos 6 meses...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs,rowNum) -> rs.getLong("id"));
        List<String> miniAllowed = getAllowedEnumValues("puntos_reclamados", "mini_juego");
        if (miniAllowed.isEmpty()) miniAllowed = MINIJUEGOS_POSSIBLES;

        for (Long hogarId : hogaresIDs) {
            int cantidad = 6 + random.nextInt(12); // 6..17 reclamaciones por hogar en 6 meses
            for (int i = 0; i < cantidad; i++) {
                LocalDateTime fecha = LocalDateTime.now().minusDays(random.nextInt(180)).withHour(random.nextInt(24)).withMinute(random.nextInt(60)).withSecond(random.nextInt(60));
                int puntos = generarPuntosReclamadosValor();
                String minijuego = miniAllowed.get(random.nextInt(miniAllowed.size()));

            String escena;
            if (!minijuego.equalsIgnoreCase("TRIVIA")) {
        
                escena = generarEscenaAleatoria();
            } else {
            
                String[] dias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
                int idx = random.nextInt(dias.length);
                escena = dias[idx];
            }

            // VALIDACIÓN FINAL DE SEGURIDAD (imposible quedar null)
            if (escena == null || escena.isBlank()) {
                escena = "Lunes"; // fallback seguro
            }


                jdbcTemplate.update("INSERT INTO puntos_reclamados (fecha, mini_juego, escena, puntos, hogar_id) VALUES (?, ?, ?, ?, ?)",
                        fecha, minijuego, escena, puntos, hogarId);

                jdbcTemplate.update("UPDATE Hogar SET puntos_disponibles = puntos_disponibles + ? WHERE id = ?", (puntos / 10), hogarId);
            }
        }
    }

    /**************
     * MÉTODOS PARA DÍA ACTUAL - (LO MÁS IMPORTANTE PARA LA DEMO)
     * - Define un hogar objetivo (por nombre si existe, sino primer hogar)
     * - Asegura 3 sectores, uno que sea PATIO con pico a las 14:00
     * - Borra mediciones de hoy para esos sectores y re-genera solo hasta la hora actual
     * - Crea evento hoy con tags (Lavado de auto en Patio)
     * - Inserta notificaciones del guión en horarios fijos
     * - Inserta puntos reclamados de hoy
     **************/
    private void insertarDatosEspecialDiaActual() {
        logger.info("Insertando datos especiales para el día actual (demo)...");

        // 1) elegir hogar objetivo preferido: "AquaVision Team" si existe, seteo aquavisiondemo
        Long hogarObjetivoId = 1L;
        try {
            hogarObjetivoId = jdbcTemplate.queryForObject("SELECT id FROM Hogar WHERE nombre = ? LIMIT 1", Long.class, "AquaVision Team");
        } catch (Exception ex) {
            // ignore
        }
        if (hogarObjetivoId == null) {
            hogarObjetivoId = jdbcTemplate.queryForObject("SELECT id FROM Hogar LIMIT 1", Long.class);
        }
        logger.info("Hogar objetivo para demo: {}", hogarObjetivoId);

        // Obtener sectores de ese hogar (asegurar que haya exactamente 3 o al menos 3)
        List<Long> sectoresHogar = jdbcTemplate.query("SELECT id FROM Sector WHERE hogar_id = ? ORDER BY id LIMIT 3", (rs, rowNum) -> rs.getLong("id"), hogarObjetivoId);

        // si no hay 3 sectores, crear sectores faltantes (muy improbable)
        int needed = 3 - sectoresHogar.size();
        if (needed > 0) {
            int currentMedidor = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id),0)+1 FROM medidores", Integer.class);
            for (int i = 0; i < needed; i++) {
                jdbcTemplate.update("INSERT INTO medidores (numero_serie, estado) VALUES (?, ?)", 200000 + random.nextInt(10000), EstadoMedidor.ON.name());
                jdbcTemplate.update("INSERT INTO Sector (nombre, categoria_sector, hogar_id, medidor_id, umbral_mensual) VALUES (?, ?, ?, ?, ?)",
                        "SECTOR_EXTRA_" + i, "OTRO", hogarObjetivoId, currentMedidor + i, 10000);
                sectoresHogar = jdbcTemplate.query("SELECT id FROM Sector WHERE hogar_id = ? ORDER BY id LIMIT 3", (rs, rowNum) -> rs.getLong("id"), hogarObjetivoId);
            }
        }

        // Elegir sector "principal" que tendrá más medición y pico
        Long sectorPrincipal = sectoresHogar.get(0);
        Long sector2 = sectoresHogar.get(1);
        Long sector3 = sectoresHogar.get(2);

        // Asegurarnos que el sector principal sea de categoria "PATIO". Si no hay patio, actualizarlo.
        String categoriaPrincipal = jdbcTemplate.queryForObject("SELECT categoria_sector FROM Sector WHERE id = ?", String.class, sectorPrincipal);
        if (categoriaPrincipal == null || !categoriaPrincipal.equalsIgnoreCase("PATIO")) {
            // Buscar si existe un sector PATIO en el hogar
            List<Long> patioSector = jdbcTemplate.query("SELECT id FROM Sector WHERE hogar_id = ? AND categoria_sector = 'PATIO' LIMIT 1", (rs, rowNum) -> rs.getLong("id"), hogarObjetivoId);
            if (!patioSector.isEmpty()) {
                sectorPrincipal = patioSector.get(0);
                // re-assign the other two if necessary
                List<Long> demás = jdbcTemplate.query("SELECT id FROM Sector WHERE hogar_id = ? AND id <> ? ORDER BY id LIMIT 2", (rs, rowNum) -> rs.getLong("id"), hogarObjetivoId, sectorPrincipal);
                if (demás.size() >= 2) {
                    sector2 = demás.get(0);
                    sector3 = demás.get(1);
                }
            } else {
                // actualizar el primer sector para que sea PATIO
                jdbcTemplate.update("UPDATE Sector SET categoria_sector = ?, nombre = ? WHERE id = ?", "PATIO", "PATIO", sectorPrincipal);
            }
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioDia = ahora.withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Borrar mediciones previas del día para los 3 sectores (evitar duplicados y garantizar control)
        jdbcTemplate.update("DELETE FROM Medicion WHERE sector_id IN (?, ?, ?) AND timestamp >= ? AND timestamp < ?", sectorPrincipal, sector2, sector3, inicioDia, inicioDia.plusDays(1));

        // 2) Insertar mediciones para hoy solo hasta la hora actual (no más allá)
        int currentHour = ahora.getHour();
        List<Object[]> batch = new ArrayList<>();
        int batchSize = 200;
        long inserted = 0;

for (int hour = 0; hour <= currentHour; hour++) {

    // ❌ Evitar generación nocturna para mediciones no realistas
    if (hour < 6) continue;

    LocalDateTime ts = inicioDia.plusHours(hour);
    Timestamp timestamp = Timestamp.valueOf(ts);

    // -----------------------
    // SECTOR PRINCIPAL (PATIO)
    // -----------------------
    int flowPrincipal;
    if (hour == 14) {
        flowPrincipal = 40 + random.nextInt(30); // pico 40-69 L/h
    } else if (hour >= 13 && hour <= 15) {
        flowPrincipal = 20 + random.nextInt(20); // actividad continua 20-39
    } else if (hour >= 10 && hour <= 12) {
        flowPrincipal = 10 + random.nextInt(10); // actividad de mañana
    } else {
        flowPrincipal = 4 + random.nextInt(7); // resto del día 4-10
    }
    batch.add(new Object[]{flowPrincipal, timestamp, sectorPrincipal});

    // -----------------------
    // SECTOR 2 (BAÑO)
    // -----------------------
    int flow2;
    if (hour >= 6 && hour <= 8) {
        flow2 = 6 + random.nextInt(10); // duchas mañana (6-15)
    } else if (hour >= 19 && hour <= 22) {
        flow2 = 4 + random.nextInt(10); // duchas noche (4-13)
    } else {
        flow2 = 0 + random.nextInt(4); // resto del día (0-3)
    }
    batch.add(new Object[]{flow2, timestamp, sector2});

    // -----------------------
    // SECTOR 3 (COCINA)
    // -----------------------
    int flow3;
    if (hour == 8 || hour == 12 || hour == 20) {
        flow3 = 6 + random.nextInt(6); // desayuno / almuerzo / cena
    } else if (hour >= 11 && hour <= 13) {
        flow3 = 3 + random.nextInt(5); // preparación almuerzo
    } else {
        flow3 = 0 + random.nextInt(3); // resto del día 0-2
    }
    batch.add(new Object[]{flow3, timestamp, sector3});

    if (batch.size() >= batchSize) {
        jdbcTemplate.batchUpdate("INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
        inserted += batch.size();
        batch.clear();
    }
}

if (!batch.isEmpty()) {
    jdbcTemplate.batchUpdate("INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", batch);
    inserted += batch.size();
}

        logger.info("Insertadas mediciones especiales para el hogar demo (hasta hora actual). total filas insertadas: {}", inserted);

        // 2.1) Asegurar que el total de hoy sea mayor que el total de ayer (para los 3 sectores)
        LocalDateTime ayerInicio = inicioDia.minusDays(1);
        LocalDateTime ayerFin = inicioDia;
        Long sumaAyer = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(flow),0) FROM Medicion WHERE sector_id IN (?, ?, ?) AND timestamp >= ? AND timestamp < ?",
                Long.class, sectorPrincipal, sector2, sector3, ayerInicio, ayerFin);

        Long sumaHoy = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(flow),0) FROM Medicion WHERE sector_id IN (?, ?, ?) AND timestamp >= ? AND timestamp < ?",
                Long.class, sectorPrincipal, sector2, sector3, inicioDia, inicioDia.plusDays(1));

        if (sumaHoy <= sumaAyer) {
            long delta = sumaAyer - sumaHoy + 5; // asegurar que hoy > ayer por al menos 5
            // aumentar el pico en sectorPrincipal a las 14:00 si existe, sino aumentar la última hora insertada
            LocalDateTime picoTime = inicioDia.plusHours(14);
            if (picoTime.isAfter(ahora)) {
                // si todavía no llegó la 14 (demo en horario anterior), aumentar la última hora registrada
                picoTime = inicioDia.plusHours(currentHour);
            }
            int rows = jdbcTemplate.update("UPDATE Medicion SET flow = flow + ? WHERE sector_id = ? AND timestamp = ?", delta, sectorPrincipal, Timestamp.valueOf(picoTime));
            if (rows == 0) {
                // si no actualizó (por algún motivo), insertar un registro adicional en la hora actual
                jdbcTemplate.update("INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)", (int)delta, Timestamp.valueOf(ahora.withMinute(0).withSecond(0).withNano(0)), sectorPrincipal);
            }
            logger.info("Ajustadas mediciones para garantizar hoy > ayer: delta aplicado {} en sector {}", delta, sectorPrincipal);
        }

        // 3) Insertar evento hoy que explique el pico (ej: "Lavado de auto" en Patio)
        LocalDateTime eventoInicio = inicioDia.plusHours(14);
        LocalDateTime eventoFin = eventoInicio.plusHours(1);
        double costo = 10.0;
        int litros = 60;

        String tituloEvento = "Lavado de auto";
        String descripcion = "Lavado de auto puntual (explicación del pico)";

        jdbcTemplate.update("INSERT INTO aqua_evento (costo, litros_consumidos, fecha_inicio, fecha_fin, sector_id, descripcion, titulo, estado_evento) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                costo, litros, eventoInicio, eventoFin, sectorPrincipal, descripcion, tituloEvento, "FINALIZADO");
        Long nuevoEventoId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id),0) FROM aqua_evento", Long.class);

        // asignar tags al evento de hoy (ej: 1 y 2) — si no existen tags con esos ids puede fallar; asumimos que sí
        try {
            jdbcTemplate.update("INSERT INTO evento_tags (evento_id, tag_id) VALUES (?, ?)", nuevoEventoId, 1);
            jdbcTemplate.update("INSERT INTO evento_tags (evento_id, tag_id) VALUES (?, ?)", nuevoEventoId, 2);
        } catch (Exception ex) {
            logger.warn("No se pudieron asignar tags al evento demo (verificar existencia de tags 1 y 2): {}", ex.getMessage());
        }

        // 4) Insertar notificaciones del guión para este hogar (fechas fijas)
        // Horarios fijos:
        // - Tendencia: 09:10
        // - Predicción disponible: 09:12
        // - Juegos pendientes: 09:15
        // - Sensor intermitente: 09:20
        // - Pico inusual detectado: 14:00
        // - Actividad registrada: 14:15 (creación de evento)
        LocalDate today = ahora.toLocalDate();
        LocalDateTime tTendencia = LocalDateTime.of(today, LocalTime.of(9, 10));
        LocalDateTime tPrediccion = LocalDateTime.of(today, LocalTime.of(9, 12));
        LocalDateTime tJuegos = LocalDateTime.of(today, LocalTime.of(9, 15));
        LocalDateTime tSensor = LocalDateTime.of(today, LocalTime.of(9, 20));
        LocalDateTime tPico = LocalDateTime.of(today, LocalTime.of(14, 0));
        LocalDateTime tActividad = LocalDateTime.of(today, LocalTime.of(14, 15));

        // eliminar notificaciones previas del hogar demo para evitar duplicados
        jdbcTemplate.update("DELETE FROM Notificacion WHERE hogar_id = ? AND fecha_envio >= ? AND fecha_envio < ?", hogarObjetivoId, inicioDia, inicioDia.plusDays(1));


        jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                hogarObjetivoId, "Notificación de predicción: posible sobrepaso del objetivo del mes", tPrediccion, "Predicción disponible", false, TipoNotificacion.INFORME.name());

        jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                hogarObjetivoId, "Juegos pendientes del día: tenés trivias por jugar", tJuegos, "Juegos pendientes", false, TipoNotificacion.INFORME.name());


        // Notificación del pico exactamente a las 14:00
        jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                hogarObjetivoId, "Pico inusual detectado en Patio", tPico, "Pico inusual detectado", false, TipoNotificacion.ALERTA.name());

        jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                hogarObjetivoId,
                "El sector Patio presenta un aumento inusual en su consumo respecto al promedio del día.",
                tTendencia,
                "Aumento inusual en Patio",
                false,
                TipoNotificacion.INFORME.name());

        jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
                hogarObjetivoId,
                "Advertencia: tu ritmo actual de consumo podría superar el umbral mensual estimado.",
                tTendencia,
                "Tendencia creciente detectada",
                false,
                TipoNotificacion.INFORME.name());

        // Notificación de creación de actividad a las 14:15 (según tu requerimiento)
        //jdbcTemplate.update("INSERT INTO Notificacion (hogar_id, mensaje, fecha_envio, titulo, leido, tipo) VALUES (?, ?, ?, ?, ?, ?)",
              //  hogarObjetivoId, "Registro de actividad: 'Lavado de auto' creado por el usuario", tActividad, "Actividad registrada", false, TipoNotificacion.INFORME.name());

        logger.info("Notificaciones especiales insertadas para hogar {} en horarios fijos (demo).", hogarObjetivoId);

        // 5) Insertar puntos reclamados hoy para el hogar objetivo (pocos: 1-3)
        List<String> miniAllowed = getAllowedEnumValues("puntos_reclamados", "mini_juego");
        if (miniAllowed.isEmpty()) miniAllowed = MINIJUEGOS_POSSIBLES;

        int cantidadPuntosHoy = 1 + random.nextInt(3);
        for (int i = 0; i < cantidadPuntosHoy; i++) {
            LocalDateTime fecha = LocalDateTime.now().minusMinutes(random.nextInt(60));
            int puntos = 10 + random.nextInt(90); // 10..99
            String minijuego = miniAllowed.get(random.nextInt(miniAllowed.size()));
            String escena = generarEscenaAleatoria();

            jdbcTemplate.update("INSERT INTO puntos_reclamados (fecha, mini_juego, escena, puntos, hogar_id) VALUES (?, ?, ?, ?, ?)",
                    fecha, minijuego, escena, puntos, hogarObjetivoId);
            jdbcTemplate.update("UPDATE Hogar SET puntos_disponibles = puntos_disponibles + ? WHERE id = ?", (puntos / 10), hogarObjetivoId);
        }

        logger.info("Datos especiales para demo insertados: eventos hoy, notificaciones y puntos reclamados para hogar {}", hogarObjetivoId);
    }


    /**************
     * ROLES
     **************/
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

    /**************
     * PUNTOS RECLAMADOS helper (6 meses generador usado arriba)
     **************/
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

    /**************
     * USUARIOS
     **************/
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

    /**************
     * ASIGNAR MEDALLAS/LOGROS
     **************/
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

    private void asignarMedallasYLogrosRandomly() {
        logger.info("Asignando medallas y logros aleatoriamente a hogares...");

        List<Long> hogaresIDs = jdbcTemplate.query("SELECT id FROM Hogar", (rs, rowNum) -> rs.getLong("id"));
        List<Long> medallaIds = jdbcTemplate.query("SELECT id FROM Medalla", (rs, rowNum) -> rs.getLong("id"));
        List<Long> logroIds = jdbcTemplate.query("SELECT id FROM Logro", (rs, rowNum) -> rs.getLong("id"));

        for (Long hogarId : hogaresIDs) {
            int medallasCount = random.nextInt(Math.min(3, medallaIds.size()) + 1);
            Collections.shuffle(medallaIds, random);
            for (int i = 0; i < medallasCount; i++) {
                jdbcTemplate.update("INSERT INTO hogar_medallas (hogar_id, medalla_id) VALUES (?, ?);", hogarId, medallaIds.get(i));
            }

            int logrosCount = random.nextInt(Math.min(3, logroIds.size()) + 1);
            Collections.shuffle(logroIds, random);
            for (int i = 0; i < logrosCount; i++) {
                jdbcTemplate.update("INSERT INTO hogar_logros (hogar_id, logro_id) VALUES (?, ?);", hogarId, logroIds.get(i));
            }
        }
    }


    private boolean datosMockInsertados() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Hogar", Integer.class);
        return count != null && count > 0;
    }

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
            if (columnType.toLowerCase().startsWith("enum(") && columnType.endsWith(")")) {
                String inner = columnType.substring(columnType.indexOf("(") + 1, columnType.lastIndexOf(")"));
                String[] parts = inner.split("','");
                List<String> values = new ArrayList<>();
                for (int i = 0; i < parts.length; i++) {
                    String p = parts[i].replaceAll("^'+", "").replaceAll("'+$", "");
                    values.add(p);
                }
                return values;
            } else {
                return Collections.emptyList();
            }
        } catch (Exception ex) {
            logger.warn("No se pudieron obtener valores permitidos para {}.{} -> {}", tableName, columnName, ex.getMessage());
            return Collections.emptyList();
        }
    }

private int generateRealisticFlow(LocalDateTime time, Long sectorId, Random random) {

    // Primero traemos el nombre del sector para reglas específicas
    String sectorName = jdbcTemplate.queryForObject(
        "SELECT nombre FROM Sector WHERE id = ?",
        String.class,
        sectorId
    );

    int hour = time.getHour();
    DayOfWeek day = time.getDayOfWeek();

    // --- 🔥 REGLAS NOCTURNAS POR SECTOR ---
    if (sectorName != null) {
        String s = sectorName.toLowerCase();

        // 🛁 Baño: sin actividad 00:00 - 05:59
        if (s.contains("baño") || s.contains("bano")) {
            if (hour >= 0 && hour <= 5) {
                return 0;
            }
        }

        // 🍳 Cocina: sin actividad 00:00 - 06:59
        if (s.contains("cocina")) {
            if (hour >= 0 && hour <= 6) {
                return 0;
            }
        }

        // 🌳 Patio/Jardín: actividad mínima nocturna, pero no 0 constante
        if (s.contains("patio") || s.contains("jardin") || s.contains("jardín")) {
            if (hour >= 0 && hour <= 5) {
                return random.nextInt(2); // 0-1, realista
            }
        }
    }

    // -------------------------
    // Horarios / consumo diario
    // -------------------------
    int minFlow;
    int maxFlow;

    if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
        // ----- DÍAS LABORALES -----
        if (hour >= 6 && hour <= 8) { minFlow = 5; maxFlow = 15; }     // mañana
        else if (hour >= 12 && hour <= 13) { minFlow = 2; maxFlow = 8; }  // almuerzo
        else if (hour >= 19 && hour <= 21) { minFlow = 8; maxFlow = 20; } // cena
        else if (hour >= 9 && hour <= 18) { minFlow = 0; maxFlow = 3; }   // actividad baja
        else { minFlow = 0; maxFlow = 1; }                               // muy bajo
    } else {
        // ----- FINES DE SEMANA -----
        if (hour >= 8 && hour <= 10) { minFlow = 4; maxFlow = 12; }
        else if (hour >= 14 && hour <= 16) { minFlow = 5; maxFlow = 15; }
        else if (hour >= 19 && hour <= 22) { minFlow = 10; maxFlow = 25; }
        else if ((hour >= 11 && hour <= 13) || (hour >= 17 && hour <= 18)) {
            minFlow = 3; maxFlow = 10;
        }
        else { minFlow = 0; maxFlow = 1; }
    }

    if (minFlow > maxFlow) {
        maxFlow = minFlow;
    }

    int baseFlow = random.nextInt(maxFlow - minFlow + 1) + minFlow;
    int noise = random.nextInt(3) - 1; //[-1,0,1]

    return Math.max(0, Math.min(120, baseFlow + noise));
}

}
