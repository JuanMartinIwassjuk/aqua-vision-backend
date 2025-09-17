package com.app.aquavision.services;

import com.app.aquavision.dto.MedicionDTO;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.repositories.MedicionRepository;
import com.app.aquavision.repositories.SectorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

@Service
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttSubscriberService {

    @Autowired
    private MedicionRepository medicionRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String broker;
    private String clientId;
    private String username;
    private String password;
    private String topic;

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriberService.class);

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

            client.subscribe(topic, (topic, message) -> {
                String payload = new String(message.getPayload());
                try {
                    logger.info("📥 Mensaje recibido en el topic {}: {}", topic, payload);
                    MedicionDTO dto = objectMapper.readValue(payload, MedicionDTO.class);
                    logger.info("📦 DTO mapeado: "
                            + "id_sector " + dto.getSectorId()
                            + ", Flujo: " + dto.getFlow()
                            + ", Timestamp: " + dto.getTimestamp().toString());

                    sectorRepository.findById(dto.getSectorId()).ifPresentOrElse(sector -> {

                        Medicion medicion = new Medicion();
                        medicion.setSector(sector);
                        medicion.setFlow(dto.getFlow());
                        medicion.setTimestamp(dto.getTimestamp());

                        medicionRepository.save(medicion);
                        logger.info("✅ Medición guardada con sector ID: {}", dto.getSectorId());
                    }, () -> {
                        logger.error("❌ Sector con ID {} no existe. Medición NO guardada.", dto.getSectorId());
                    });

                } catch (Exception e) {
                    logger.error("❌ Error al mapear o guardar la medición: {}", e.getMessage());
                }
            });

            logger.info("✅ Suscrito a {} con éxito.", topic);

            //TODO: Agregar validacion de datos recibidos y enviar notificaciones

        } catch (MqttException e) {
            logger.error("❌ Error al conectar al broker MQTT: {}", e.getMessage());
        }
    }

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
