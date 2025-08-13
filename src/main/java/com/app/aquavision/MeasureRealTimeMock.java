package com.app.aquavision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

public class MeasureRealTimeMock {

    private static final Logger logger = LoggerFactory.getLogger(MeasureRealTimeMock.class);

    public static void main(String[] args) {
        try {
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USERNAME");
            String pass = System.getenv("DB_PASSWORD");
            int ejecucion = 1;

            Random random = new Random();
            
            List<String> usuarios = List.of("matif", "agus", "juan", "matip", "erik");

            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
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
                    logger.info("--------------------------------------------------------------------------------------------");
                    logger.info("------------------------------------- Ejecucion N°: {} --------------------------------------", ejecucion);
                    logger.info("--------------------------------------------------------------------------------------------");
                    for (Map.Entry<String, List<SectorInfo>> entry : sectoresPorUsuario.entrySet()) {
                        List<SectorInfo> sectores = entry.getValue();

                        for (SectorInfo sector : sectores) {
                            int flow = medicionRandom(random);

                            try (PreparedStatement stmt = conn.prepareStatement(
                                    "INSERT INTO Medicion (flow, timestamp, sector_id) VALUES (?, ?, ?)")) {
                                stmt.setInt(1, flow);
                                stmt.setTimestamp(2, ts);
                                stmt.setLong(3, sector.id);
                                stmt.executeUpdate();


                            logger.info("Caudal registrado: {} m³ --- Usuario: {} --- Sector: {} -- ID Hogar {} -- Fecha: {}",
                                        flow, sector.username, sector.categoria, sector.hogarId, ts);
                            }

                        }
                        logger.info("----------------------------------------------------------------------------------");
                    }
                    ejecucion++;
                    Thread.sleep(5_000); // cada 10 segundos
                }
            }
        } catch (Exception e) {
            logger.error("Error ejecutando mock", e);
        }
    }

    private static List<SectorInfo> getSectorInfoForUser(Connection conn, String username) {
        String sql = """
            SELECT s.id, s.categoria, h.id AS hogar_id, u.username
            FROM Sector s
            JOIN Hogar h ON s.hogar_id = h.id
            JOIN User_ u ON u.hogar_id = h.id
            WHERE u.username = ?
        """;
        List<SectorInfo> sectores = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            var rs = stmt.executeQuery();
            while (rs.next()) {
                sectores.add(new SectorInfo(
                        rs.getLong("id"),
                        rs.getString("categoria"),
                        rs.getLong("hogar_id"),
                        rs.getString("username")
                ));
            }
        } catch (Exception e) {
            logger.error("Error obteniendo sectores del usuario {}", username, e);
        }
        return sectores;
    }

    private static int medicionRandom(Random random) {
        if (random.nextDouble() < 0.1) {
            return 0;
        }
        return random.nextInt(99) + 1;
    }

    private record SectorInfo(Long id, String categoria, Long hogarId, String username) {}
}
