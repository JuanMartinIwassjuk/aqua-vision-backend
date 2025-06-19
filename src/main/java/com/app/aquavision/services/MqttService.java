package com.app.aquavision.services;

import javax.net.ssl.SSLSocketFactory;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.stereotype.Service;



@Service
public class MqttService {

    private final String broker = "ssl://m171d340.ala.us-east-1.emqxsl.com:8883";
    private final String clientId = "spring-mqtt-subscriber";
    private final String topic = "mediciones";
    private final String username = "test";
    private final String password = "test123";

    private MqttClient client;

    @PostConstruct
    public void start() {
        try {
            if (client == null) {
                client = new MqttClient(broker, clientId, null);
                client.setCallback(new SimpleCallback());
            }

            if (!client.isConnected()) {
                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                options.setSocketFactory(SSLSocketFactory.getDefault());

                client.connect(options);
                System.out.println("✅ Conectado a EMQX");

                // ✅ Solo te suscribís si estás conectado
                client.subscribe(topic);
                System.out.println("📡 Suscrito a: " + topic);
            }
        } catch (MqttException e) {
            System.err.println("🚫 Error al conectar o suscribirse:");
            e.printStackTrace();
        }
    }

    private class SimpleCallback implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {
            System.out.println("❌ Conexión perdida: " + cause.getMessage());

            // Intentamos reconectar después de 5 segundos
            new Thread(() -> {
                while (!client.isConnected()) {
                    try {
                        Thread.sleep(5000);
                        System.out.println("🔄 Reintentando conexión...");
                        start(); // Vuelve a intentar conectar y suscribirse si no está ya
                    } catch (Exception e) {
                        System.out.println("⏳ Fallo en reconexión. Intentando de nuevo...");
                    }
                }
            }).start();
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            System.out.println("📩 Mensaje recibido:");
            System.out.println("➡ Tópico: " + topic);
            System.out.println("➡ Contenido: " + new String(message.getPayload()));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {}
    }
}
