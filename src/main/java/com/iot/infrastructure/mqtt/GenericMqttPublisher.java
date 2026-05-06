package com.iot.infrastructure.mqtt;

import com.iot.domain.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Generic MQTT implementation for simulation.
 * Acts as a local in-memory broker to allow devices to communicate during simulation.
 */
public class GenericMqttPublisher extends AbstractMqttAdapter {
    private static final Map<String, List<Consumer<Message>>> subscribers = new ConcurrentHashMap<>();

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
        
        subscribers.forEach((topicPattern, callbacks) -> {
            if (matches(topicPattern, message.getTopic())) {
                callbacks.forEach(callback -> callback.accept(message));
            }
        });
    }

    @Override
    protected void performSubscribe(String topic, Consumer<Message> callback) throws Exception {
        System.out.println("[Traffic-Broker] SUBSCRIBE to " + topic);
        subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(callback);
    }

    @Override
    protected void performUnsubscribe(String topic) throws Exception {
        System.out.println("[Traffic-Broker] UNSUBSCRIBE from " + topic);
        subscribers.remove(topic);
    }

    private boolean matches(String pattern, String topic) {
        if (pattern.equals(topic)) return true;
        if (pattern.contains("+")) {
            String regex = pattern.replace("+", "[^/]+").replace("/", "\\/");
            return topic.matches(regex);
        }
        return false;
    }

    @Override
    public void close() throws Exception {
        System.out.println("[Traffic-Broker] Closing simulated connection.");
    }
}
