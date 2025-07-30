package com.app.aquavision.services;

import com.app.aquavision.entities.domain.Medicion;
import com.app.aquavision.repositories.MedicionRepository;
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
                    Medicion medicion = objectMapper.readValue(payload, Medicion.class);
                    medicionRepository.save(medicion);
                    //TODO: falta mapear el sector de la medicion
                    System.out.println("✅ Medición guardada en la base de datos.");
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
