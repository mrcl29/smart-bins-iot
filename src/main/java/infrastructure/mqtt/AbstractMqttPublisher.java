package infrastructure.mqtt;

import core.Message;
import core.MessagePublisher;

/**
 * Base class for MQTT-based message publishers.
 * Provides common functionality for connection validation and logging.
 */
public abstract class AbstractMqttPublisher implements MessagePublisher {
    protected final String brokerUrl;
    protected final String clientId;

    protected AbstractMqttPublisher(String brokerUrl, String clientId) {
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

    /**
     * Ensures that the connection to the broker is active.
     * @throws Exception if connection is not established or fails.
     */
    protected abstract void ensureConnected() throws Exception;

    /**
     * Performs the actual publishing of the message.
     * @param message The message to publish.
     * @throws Exception if publishing fails.
     */
    protected abstract void performPublish(Message message) throws Exception;

    @Override
    public abstract void close() throws Exception;
}
