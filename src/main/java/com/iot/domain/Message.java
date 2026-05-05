// src/core/Message.java
package com.iot.domain;

/**
 * Standard Data Transfer Object (DTO) for IoT communication.
 */
public class Message {
    private final String topic;
    private final String payload;

    /**
     * Constructs a new IoT Message.
     * 
     * @param topic   The destination topic or endpoint.
     * @param payload The data payload (usually JSON).
     */
    public Message(String topic, String payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public String getPayload() {
        return payload;
    }
}
