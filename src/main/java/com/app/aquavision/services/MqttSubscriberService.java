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
import org.springframework.stereotype.Service;

@Service
public class MqttSubscriberService {
    @Autowired
    private MedicionRepository medicionRepository;

    @Autowired
    private SectorRepository sectorRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String BROKER = "ssl://m171d340.ala.us-east-1.emqxsl.com:8883";
    private static final String CLIENT_ID = "spring-backend-listener";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test123"; //TODO: Mover a envs
    private static final String TOPIC = "mediciones";

    private static final Logger logger = LoggerFactory.getLogger(MqttSubscriberService.class);

    @PostConstruct
    public void start() {



        try {
            MqttClient client = new MqttClient(BROKER, CLIENT_ID, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);
            options.setKeepAliveInterval(60);
            options.setSocketFactory(SSLSocketFactoryGenerator.getSocketFactory());

            client.connect(options);

            client.subscribe(TOPIC, (topic, message) -> {
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

            logger.info("✅ Suscrito a {} con éxito.", TOPIC);

            //TODO: Agregar validacion de datos recibidos y enviar notificaciones

        } catch (MqttException e) {
            logger.error("❌ Error al conectar al broker MQTT: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
