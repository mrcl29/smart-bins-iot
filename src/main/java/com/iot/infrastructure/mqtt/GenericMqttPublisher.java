package com.iot.infrastructure.mqtt;

import com.iot.domain.Message;
import java.util.function.Consumer;

/**
 * Generic MQTT implementation of the MQTT adapter (Stub).
 */
public class GenericMqttPublisher extends AbstractMqttAdapter {

    public GenericMqttPublisher(String brokerUrl, String clientId) {
        super(brokerUrl, clientId);
    }

    @Override
    protected void ensureConnected() throws Exception {
        // Implementation for standard MQTT connection
    }

    @Override
    protected void performPublish(Message message) throws Exception {
        // Implementation for publishing
    }

    @Override
    protected void performSubscribe(String topic, Consumer<Message> callback) throws Exception {
        // Implementation for subscribing
    }

    @Override
    protected void performUnsubscribe(String topic) throws Exception {
        // Implementation for unsubscribing
    }

    @Override
    public void close() throws Exception {
        System.out.println("Closing generic MQTT connection.");
    }
}
