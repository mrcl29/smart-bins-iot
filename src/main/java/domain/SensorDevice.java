// src/domain/SensorDevice.java
package domain;

import core.Message;
import core.MessagePublisher;

import java.util.Locale;

/**
 * Represents a generic sensor device in the IoT ecosystem.
 */
public class SensorDevice {
    private final String deviceId;
    private final MessagePublisher publisher;

    /**
     * Injects the communication dependency into the device.
     * 
     * @param deviceId  Unique identifier for the device.
     * @param publisher The protocol implementation used for sending data.
     */
    public SensorDevice(String deviceId, MessagePublisher publisher) {
        this.deviceId = deviceId;
        this.publisher = publisher;
    }

    /**
     * Reads sensor data and publishes it.
     * 
     * @param reading Value read by the physical sensor.
     */
    public void sendTelemetry(double reading) {
        String topic = "telemetry/" + this.deviceId;
        String jsonPayload = String.format(Locale.US, "{\"deviceId\":\"%s\", \"value\":%.2f}", this.deviceId, reading);

        Message msg = new Message(topic, jsonPayload);
        try {
            this.publisher.publish(msg);
            System.out.println("Telemetry sent successfully for device: " + this.deviceId);
        } catch (Exception e) {
            System.err.println("Failed to send telemetry: " + e.getMessage());
            // Here you could implement a retry mechanism or local storage (Circuit Breaker
            // pattern)
        }
    }
}
