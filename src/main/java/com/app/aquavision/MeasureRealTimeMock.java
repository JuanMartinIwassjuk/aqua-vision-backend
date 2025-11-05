package com.app.aquavision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class MeasureRealTimeMock {

    private static final Logger logger = LoggerFactory.getLogger(MeasureRealTimeMock.class);

    private static final long periodoEjecucion = 10_000; //segundos
    //private static final float probablidadDeMedicion = 0.1f; //segundos

    private static final String url = System.getenv("DB_URL");

    public static void main(String[] args) {
        try {

            int nro_ejecucion = 1;
            List<String> usuarios = List.of("matif", "agus", "juan", "matip", "erik");

            try (Connection conn = DriverManager.getConnection(url)) {
                logger.info("Conexión con DB establecida correctamente");

                Map<String, List<SectorInfo>> sectoresPorUsuario = new HashMap<>();
                for (String usuarioSimulado : usuarios) {
                    List<SectorInfo> sectores = getSectorInfoForUser(conn, usuarioSimulado);
                    if (sectores.isEmpty()) {
                        logger.warn("No se encontraron sectores para el usuario {}", usuarioSimulado);
                    } else {
                        sectoresPorUsuario.put(usuarioSimulado, sectores);
                    }
                }

                while (true) {
                    Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
                    loggearEjecucion(nro_ejecucion);

                    for (Map.Entry<String, List<SectorInfo>> entry : sectoresPorUsuario.entrySet()) {
                        List<SectorInfo> sectores = entry.getValue();

                        for (SectorInfo sector : sectores) {
                            int flow = obtenerMedicion();

                            try (PreparedStatement stmt = conn.prepareStatement(
                                    "INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)")) {
                                stmt.setInt(1, flow);
                                stmt.setTimestamp(2, ts);
                                stmt.setLong(3, sector.id);
                                stmt.executeUpdate();

                                logger.info("Caudal registrado: {} m³ --- Usuario: {} --- Sector: {} -- ID Hogar {} -- Fecha: {}",
                                        flow, sector.username, sector.categoriaSector, sector.hogarId, ts);
                            }
                        }
                        logger.info("----------------------------------------------------------------------------------");
                    }
                    nro_ejecucion++;
                    Thread.sleep(periodoEjecucion);
                }
            }
        } catch (Exception e) {
            logger.error("Error ejecutando mock: {}", e.getMessage());
        }
    }

    private static List<SectorInfo> getSectorInfoForUser(Connection conn, String username) {
        String sql = """
            SELECT s.id, s.categoria_sector, h.id AS hogar_id, u.username
            FROM sector s
            JOIN hogar h ON s.hogar_id = h.id
            JOIN user_ u ON u.hogar_id = h.id
            WHERE u.username = ?
        """;
        List<SectorInfo> sectores = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                sectores.add(new SectorInfo(
                        rs.getLong("id"),
                        rs.getString("categoria_sector"),
                        rs.getLong("hogar_id"),
                        rs.getString("username")
                ));
            }
        } catch (Exception e) {
            logger.error("Error obteniendo sectores del usuario {}, {}", username, e.getMessage());
        }
        return sectores;
    }

    private static void loggearEjecucion(int ejecucion){
        logger.info("--------------------------------------------------------------------------------------------");
        logger.info("------------------------------------- Ejecucion N°: {} --------------------------------------", ejecucion);
        logger.info("--------------------------------------------------------------------------------------------");
    }

    private static int obtenerMedicion() {
        Random random = new Random();
        //if (random.nextDouble() < probablidadDeMedicion) {return 0; }
        return random.nextInt(99) + 1;
    }

    private record SectorInfo(Long id, String categoriaSector, Long hogarId, String username) {}
}
