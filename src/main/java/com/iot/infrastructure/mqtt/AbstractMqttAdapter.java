package com.iot.infrastructure.mqtt;

import com.iot.domain.Message;
import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.MessageSubscriber;
import java.util.function.Consumer;

/**
 * Base class for MQTT-based adapters.
 * Implements both publishing and subscribing capabilities.
 */
public abstract class AbstractMqttAdapter implements MessagePublisher, MessageSubscriber {
    protected final String brokerUrl;
    protected final String clientId;

    protected AbstractMqttAdapter(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    @Override
    public void publish(Message message) throws Exception {
        ensureConnected();
        performPublish(message);
        System.out.println(String.format("[%s] Published to topic: %s", 
                this.getClass().getSimpleName(), message.getTopic()));
    }

    @Override
    public void subscribe(String topic, Consumer<Message> callback) throws Exception {
        ensureConnected();
        performSubscribe(topic, callback);
        System.out.println(String.format("[%s] Subscribed to topic: %s", 
                this.getClass().getSimpleName(), topic));
    }

    @Override
    public void unsubscribe(String topic) throws Exception {
        ensureConnected();
        performUnsubscribe(topic);
        System.out.println(String.format("[%s] Unsubscribed from topic: %s", 
                this.getClass().getSimpleName(), topic));
    }

    /**
     * Ensures that the connection to the broker is active.
     */
    protected abstract void ensureConnected() throws Exception;

    protected abstract void performPublish(Message message) throws Exception;

    protected abstract void performSubscribe(String topic, Consumer<Message> callback) throws Exception;

    protected abstract void performUnsubscribe(String topic) throws Exception;

    @Override
    public abstract void close() throws Exception;
}
