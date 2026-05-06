package com.iot.infrastructure.mqtt;

import com.iot.domain.Message;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Real Paho MQTT implementation for Smart Traffic communication.
 */
public class PahoMqttAdapter extends AbstractMqttAdapter {
    private MqttClient client;
    private final MqttConnectOptions options;

    public PahoMqttAdapter(String brokerUrl, String clientId) {
        super(brokerUrl, clientId);
        this.options = new MqttConnectOptions();
        this.options.setCleanSession(true);
        this.options.setAutomaticReconnect(true);
        this.options.setConnectionTimeout(10);
        
        try {
            this.client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
            this.connect();
        } catch (MqttException e) {
            System.err.println("Error initializing Paho MQTT Client: " + e.getMessage());
        }
    }

    private void connect() {
        try {
            if (!client.isConnected()) {
                client.connect(options);
                System.out.println("Connected to MQTT Broker: " + brokerUrl);
            }
        } catch (MqttException e) {
            System.err.println("Failed to connect to MQTT Broker: " + e.getMessage());
        }
    }

    @Override
    protected void ensureConnected() throws Exception {
        if (client == null || !client.isConnected()) {
            connect();
        }
    }

    @Override
    protected void performPublish(Message message) throws Exception {
        MqttMessage mqttMessage = new MqttMessage(message.getPayload().getBytes(StandardCharsets.UTF_8));
        mqttMessage.setQos(1);
        client.publish(message.getTopic(), mqttMessage);
    }

    @Override
    protected void performSubscribe(String topic, Consumer<Message> callback) throws Exception {
        client.subscribe(topic, (t, m) -> {
            String payload = new String(m.getPayload(), StandardCharsets.UTF_8);
            callback.accept(new Message(t, payload));
        });
    }

    @Override
    protected void performUnsubscribe(String topic) throws Exception {
        client.unsubscribe(topic);
    }

    @Override
    public void close() throws Exception {
        if (client != null && client.isConnected()) {
            client.disconnect();
            client.close();
        }
    }
}
