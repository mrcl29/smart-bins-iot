package com.iot.ports.out;

import com.iot.domain.Message;
import java.util.function.Consumer;

/**
 * Interface defining the contract for subscribing to messages.
 */
public interface MessageSubscriber {
    /**
     * Subscribes to a specific topic.
     * 
     * @param topic    The topic to subscribe to.
     * @param callback The callback to execute when a message is received.
     */
    void subscribe(String topic, Consumer<Message> callback) throws Exception;

    /**
     * Unsubscribes from a specific topic.
     * 
     * @param topic The topic to unsubscribe from.
     */
    void unsubscribe(String topic) throws Exception;
}
