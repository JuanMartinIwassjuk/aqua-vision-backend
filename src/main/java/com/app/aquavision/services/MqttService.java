package com.app.aquavision.services;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class MqttService {
    private static final String HOST = "19f34eb54e474d07903d662fd9f2e410.s1.eu.hivemq.cloud";
    private static final String USERNAME = "leandro";
    private static final String PASSWORD = "12345Erik";
    private static final String TOPIC = "casa1/agua";
    private static final String FILE_PATH = "mqtt_messages.txt"; // Ruta del archivo

    private Mqtt5AsyncClient client;

    @PostConstruct
    public void connectAndSubscribe() {
        client = MqttClient.builder()
                .identifier("java-console-test")
                .serverHost(HOST)
                .serverPort(8883) // TLS port
                .sslWithDefaultConfig()
                .useMqttVersion5()
                .buildAsync();

        client.connectWith()
                .cleanStart(true)
                .simpleAuth()
                .username(USERNAME)
                .password(StandardCharsets.UTF_8.encode(PASSWORD))
                .applySimpleAuth()
                .send()
                .thenAccept(connAck -> {
                    System.out.println("✅ Connected to HiveMQ Cloud");

                    // Subscribe to the topic
                    client.subscribeWith()
                            .topicFilter(TOPIC)
                            .send()
                            .thenAccept(subAck -> System.out.println("✅ Subscribed to topic: " + TOPIC));

                    // Handle incoming messages
                    client.publishes(MqttGlobalPublishFilter.ALL, publish -> {
                        String topic = publish.getTopic().toString();
                        String message = StandardCharsets.UTF_8.decode(publish.getPayload().get()).toString();

                        System.out.println("\n📥 Message received:");
                        System.out.println("Topic: " + topic);
                        System.out.println("Payload: " + message);

                        // Save the message to a file
                        saveMessageToFile(topic, message);
                    });
                })
                .exceptionally(throwable -> {
                    System.err.println("❌ Connection failed: " + throwable.getMessage());
                    return null;
                });
    }

    private void saveMessageToFile(String topic, String message) {
        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            writer.write("Topic: " + topic + "\n");
            writer.write("Payload: " + message + "\n");
            writer.write("-----\n");
        } catch (IOException e) {
            System.err.println("❌ Error writing to file: " + e.getMessage());
        }
    }
}