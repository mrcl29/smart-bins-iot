// src/core/MessagePublisher.java
package com.iot.ports.out;

import com.iot.domain.Message;

/**
 * Interface defining the contract for publishing messages.
 * This abstracts away the underlying protocol (MQTT, REST, etc.).
 */
public interface MessagePublisher extends AutoCloseable {
    /**
     * Publishes a message to the configured broker/server.
     * 
     * @param message The message object containing topic and payload.
     * @throws Exception if the transmission fails.
     */
    void publish(Message message) throws Exception;

    @Override
    default void close() throws Exception {
        // Default implementation does nothing
    }
}
