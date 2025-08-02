package com.app.aquavision.services;

import com.app.aquavision.dto.MedicionDTO;
import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.repositories.MedicionRepository;
import com.app.aquavision.repositories.SectorRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
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
    private static final String PASSWORD = "test123"; //habria que moverlo tambien al .env.local estas credenciales
    private static final String TOPIC = "mediciones";

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
                    System.out.println("📥 Mensaje recibido en el topic " + topic + ": " + payload);
                    MedicionDTO dto = objectMapper.readValue(payload, MedicionDTO.class);
                    System.out.println("📦 DTO mapeado: " +
                            "id_sector " + dto.getSectorId()
                    + ", Flujo: " + dto.getFlow()
                            + ", Timestamp: " + dto.getTimestamp().toString());

                    sectorRepository.findById(dto.getSectorId()).ifPresentOrElse(sector -> {

                        Medicion medicion = new Medicion();
                        medicion.setSector(sector);
                        medicion.setFlow(dto.getFlow());
                        medicion.setTimestamp(dto.getTimestamp());

                        medicionRepository.save(medicion);
                        System.out.println("✅ Medición guardada con sector ID: " + dto.getSectorId());
                    }, () -> {
                        System.err.println("❌ Sector con ID " + dto.getSectorId() + " no existe. Medición NO guardada.");
                    });

                } catch (Exception e) {
                    System.err.println("❌ Error al mapear o guardar: " + e.getMessage());
                }
            });

            System.out.println("✅ Suscrito a " + TOPIC + " con éxito.");

        } catch (MqttException e) {
            System.err.println("❌ Error al conectar al broker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
