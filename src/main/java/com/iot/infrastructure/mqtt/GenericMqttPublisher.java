package com.iot.infrastructure.mqtt;

import com.iot.domain.Message;
import java.util.function.Consumer;

/**
 * Generic MQTT implementation for simulation (Stub).
 * Logs all operations to the console to visualize traffic.
 */
public class GenericMqttPublisher extends AbstractMqttAdapter {

    public GenericMqttPublisher(String brokerUrl, String clientId) {
        super(brokerUrl, clientId);
        System.out.println("[Traffic-Broker] Initialized simulator for " + brokerUrl);
    }

    @Override
    protected void ensureConnected() throws Exception {
        // Simulated connection
    }

    @Override
    protected void performPublish(Message message) throws Exception {
        System.out.println("[Traffic-Broker] PUBLISH to " + message.getTopic() + ": " + message.getPayload());
    }

    @Override
    protected void performSubscribe(String topic, Consumer<Message> callback) throws Exception {
        System.out.println("[Traffic-Broker] SUBSCRIBE to " + topic);
    }

    @Override
    protected void performUnsubscribe(String topic) throws Exception {
        System.out.println("[Traffic-Broker] UNSUBSCRIBE from " + topic);
    }

    @Override
    public void close() throws Exception {
        System.out.println("[Traffic-Broker] Closing simulated connection.");
    }
}
