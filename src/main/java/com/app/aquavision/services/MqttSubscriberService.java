package com.app.aquavision.services;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;

@Service
public class MqttSubscriberService {

    private static final String BROKER = "ssl://seb99e1e.ala.us-east-1.emqxsl.com:8883";
    private static final String CLIENT_ID = "spring-backend-listener";
    private static final String USERNAME = "prueba-aquavision";
    private static final String PASSWORD = "123456"; //habria que moverlo tambien al .env.local estas credenciales
    private static final String TOPIC = "casa1/agua";

    @PostConstruct
    public void start() {
        try {
            MqttClient client = new MqttClient(BROKER, CLIENT_ID, null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(USERNAME);
            options.setPassword(PASSWORD.toCharArray());
            options.setCleanSession(true);
            options.setKeepAliveInterval(60);
            options.setSocketFactory(SSLSocketFactoryGenerator.getSocketFactory());  // ⚠️ Requiere configuración de SSL

            client.connect(options);

            client.subscribe(TOPIC, (topic, message) -> {
                String payload = new String(message.getPayload());
                System.out.println("📥 Mensaje recibido:");
                System.out.println("  • Topic: " + topic);
                System.out.println("  • Payload: " + payload);
            });

            System.out.println("✅ Suscrito a " + TOPIC + " con éxito.");

        } catch (MqttException e) {
            System.err.println("❌ Error al conectar al broker: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
