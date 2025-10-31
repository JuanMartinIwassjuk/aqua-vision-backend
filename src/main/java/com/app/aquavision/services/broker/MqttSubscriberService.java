package com.app.aquavision.services.broker;

import com.app.aquavision.dto.MedicionDTO;
import com.app.aquavision.entities.domain.EstadoMedidor;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.entities.domain.Medidor;
import com.app.aquavision.entities.domain.notifications.Notificacion;
import com.app.aquavision.entities.domain.notifications.TipoNotificacion;
import com.app.aquavision.repositories.MedicionRepository;
import com.app.aquavision.repositories.MedidorRepository;
import com.app.aquavision.repositories.SectorRepository;
import com.app.aquavision.services.NotificacionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttSubscriberService {

    @Autowired private MedicionRepository medicionRepository;
    @Autowired private SectorRepository sectorRepository;
    @Autowired private MedidorRepository medidorRepository;
    @Autowired private ObjectMapper objectMapper;

    @Autowired
    private NotificacionService notificacionService;


    private String broker;
    private String clientId;
    private String username;
    private String password;
    private String topic;

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriberService.class);

    /** Minutos seguidos con flow=0 para pasar a HIBERNATING */
    private static final int ZERO_STREAK_TO_HIBERNATE = 15;

    /** Tracker en memoria por medidor: racha de ceros */
    private static class Tracker {
        final AtomicInteger zeroStreak = new AtomicInteger(0);
    }
    /** clave = numeroSerie del Medidor */
    private final ConcurrentMap<Integer, Tracker> trackers = new ConcurrentHashMap<>();

    @PostConstruct
    public void start() {
        try {
            MqttClient client = new MqttClient(broker, clientId, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setCleanSession(true);
            options.setKeepAliveInterval(60);
            options.setSocketFactory(SSLSocketFactoryGenerator.getSocketFactory());

            client.connect(options);

            client.subscribe(topic, (top, message) -> {
                String payload = new String(message.getPayload());
                logger.info("📥 [{}]: {}", top, payload);

                try {
                    JsonNode root = objectMapper.readTree(payload);

                    // -------- (1) EVENTOS DE ESTADO: online / offline --------
                    if (root.has("evt")) {
                        String evt = root.path("evt").asText();
                        String deviceId = root.path("deviceId").asText(null);

                        if (deviceId == null || deviceId.isBlank()) {
                            logger.warn("Evento con evt pero sin deviceId. Ignorado.");
                            return;
                        }

                        int numeroSerie = parseDeviceId(deviceId);

                        medidorRepository.findByNumeroSerie(numeroSerie).ifPresentOrElse(medidor -> {
                            Tracker t = trackers.computeIfAbsent(numeroSerie, k -> new Tracker());

                            if ("online".equalsIgnoreCase(evt)) {
                                t.zeroStreak.set(0);
                                setEstadoIfChanged(medidor, EstadoMedidor.IDLE);
                                crearNotificacionCambioEstado(medidor, "El medidor se ha reconectado y está nuevamente en línea.", TipoNotificacion.INFORME);
                                logger.info("🟢 Medidor {} ONLINE", numeroSerie);

                            } else if ("offline".equalsIgnoreCase(evt)) {
                                t.zeroStreak.set(0);
                                setEstadoIfChanged(medidor, EstadoMedidor.OFFLINE);
                                crearNotificacionCambioEstado(medidor, "El medidor se ha desconectado y está fuera de línea.",TipoNotificacion.ALERTA);
                                logger.info("🔴 Medidor {} OFFLINE", numeroSerie);
                            }


                            medidorRepository.save(medidor);
                        }, () -> logger.error("❌ Medidor numeroSerie {} no encontrado", numeroSerie));

                        return; // ya procesado
                    }

                    // -------- (2) MEDICIÓN NORMAL --------
                    MedicionDTO dto = objectMapper.treeToValue(root, MedicionDTO.class);
                    // Si tu "numeroSerie" coincide con sectorId, usamos eso.
                    // Si no, mapeá acá sectorId -> numeroSerie como necesites.
                    int numeroSerie = dto.getSectorId().intValue();

                    Tracker t = trackers.computeIfAbsent(numeroSerie, k -> new Tracker());

                    medidorRepository.findByNumeroSerie(numeroSerie).ifPresent(medidor -> {
                        double flow = root.path("flow").asDouble(0.0);

                        logger.info("📦 Medición: sector={}, flow={}, ts={}",
                                dto.getSectorId(), flow, dto.getTimestamp());

                        if (flow > 0) {
                            t.zeroStreak.set(0);
                            setEstadoIfChanged(medidor, EstadoMedidor.ON);
                        } else {
                            int streak = t.zeroStreak.incrementAndGet();
                            EstadoMedidor nuevo = (streak >= ZERO_STREAK_TO_HIBERNATE)
                                    ? EstadoMedidor.HIBERNATING
                                    : EstadoMedidor.IDLE;
                            setEstadoIfChanged(medidor, nuevo);
                        }
                        medidorRepository.save(medidor);
                    });

                    // Persistir medición sólo si flow > 0 (igual que antes)
                    int flowForSave = (int) root.path("flow").asDouble(0.0);

                    if (flowForSave > 0) {
                        sectorRepository.findById(dto.getSectorId()).ifPresentOrElse(sector -> {
                            Medicion m = new Medicion();
                            m.setSector(sector);
                            m.setFlow(flowForSave);
                            m.setTimestamp(dto.getTimestamp());
                            medicionRepository.save(m);
                            logger.info("✅ Medición guardada (sector ID: {})", dto.getSectorId());
                        }, () -> logger.error("❌ Sector {} no existe. NO guardada.", dto.getSectorId()));
                    } else {
                        logger.info("➖ Flow=0: no se guarda medición (sólo presencia/estado).");
                    }

                } catch (Exception e) {
                    logger.error("❌ Error procesando mensaje: {}", e.getMessage(), e);
                }
            });

            logger.info("✅ Suscrito a {} con éxito.", topic);

        } catch (MqttException e) {
            logger.error("❌ Error al conectar al broker MQTT: {}", e.getMessage(), e);
        }
    }

    /** Si cambia el estado, lo setea y loguea (evita escribir de más). */
    private void setEstadoIfChanged(Medidor medidor, EstadoMedidor nuevo) {
        if (medidor.getEstado() != nuevo) {
            EstadoMedidor anterior = medidor.getEstado();
            medidor.setEstado(nuevo);
            logger.info("🔄 Estado medidor {} -> {} (antes: {})",
                    medidor.getNumeroSerie(), nuevo, anterior);
        }
    }

    /**
     * Convierte un deviceId tipo "esp32-44E600A7DBCC" a int numeroSerie.
     * Si tu deviceId YA ES el numeroSerie, cambiá esto por Integer.parseInt(deviceId).
     */
    private int parseDeviceId(String deviceId) {
        try {
            if (deviceId.startsWith("esp32-")) {
                String hex = deviceId.substring(6);
                // reduce a int positivo
                return Math.toIntExact(Long.parseLong(hex, 16) & 0x7FFFFFFF);
            }
            return Integer.parseInt(deviceId);
        } catch (Exception e) {
            logger.warn("No se pudo parsear deviceId {} a numeroSerie, usando hashCode", deviceId);
            return Math.abs(deviceId.hashCode());
        }
    }
    private void crearNotificacionCambioEstado(Medidor medidor, String mensaje, TipoNotificacion tipo) {
        try {
            Optional<Long> hogarOpt = sectorRepository.findHogarIdByNumeroSerie(medidor.getNumeroSerie());
            if (hogarOpt.isEmpty()) {
                logger.warn("⚠️ No se encontró hogar asociado al medidor {}.", medidor.getNumeroSerie());
                return;
            }

            Long hogarId = hogarOpt.get();
            logger.info("🏠 Medidor {} pertenece al hogar {}", medidor.getNumeroSerie(), hogarId);

            Notificacion notif = new Notificacion();
            notif.setTitulo("Estado del medidor");
            notif.setMensaje(mensaje);
            notif.setFechaEnvio(LocalDateTime.now());
            notif.setTipo(tipo);
            notif.setLeido(false);

            notificacionService.createNotification(hogarId, notif);
            logger.info("📩 Notificación creada para hogar {}: {}", hogarId, mensaje);

        } catch (Exception e) {
            logger.error("❌ Error al crear notificación para medidor {}: {}", medidor.getNumeroSerie(), e.getMessage(), e);
        }
    }


    // getters/setters para propiedades de app.mqtt
    public String getBroker() { return broker; }
    public void setBroker(String broker) { this.broker = broker; }
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }


}
