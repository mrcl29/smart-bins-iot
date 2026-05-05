package com.iot.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.domain.Message;
import com.iot.ports.out.MessagePublisher;

/**
 * Scalable Smart Bin device implementation.
 * Uses Jackson for serialization and decoupled domain models.
 */
public class SmartBinDevice extends SensorDevice {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final double alertThreshold;
    private final String wasteType;
    private final Location location;

    public SmartBinDevice(String deviceId, MessagePublisher publisher, double alertThreshold, 
                          String wasteType, Location location) {
        super(deviceId, publisher);
        this.alertThreshold = alertThreshold;
        this.wasteType = wasteType;
        this.location = location;
    }

    /**
     * Updates the fill level and sends detailed telemetry.
     * 
     * @param level Current fill level percentage.
     */
    public void updateFillLevel(double level) {
        publishMessage(level, "OK", "telemetry/bins/");
        
        if (level >= alertThreshold) {
            publishMessage(level, "CRITICAL", "alerts/bins/");
        }
    }

    private void publishMessage(double level, String status, String topicPrefix) {
        try {
            SmartBinPayload.BinMetadata metadata = new SmartBinPayload.BinMetadata(wasteType, location);
            SmartBinPayload payload = new SmartBinPayload(getDeviceId(), status, level, alertThreshold, metadata);
            
            String jsonPayload = objectMapper.writeValueAsString(payload);
            String topic = topicPrefix + getDeviceId();
            
            Message msg = new Message(topic, jsonPayload);
            getPublisher().publish(msg);
            
            System.out.println(String.format("[%s] Published to %s: %s", status, topic, wasteType));
        } catch (Exception e) {
            System.err.println("Error publishing smart bin message: " + e.getMessage());
        }
    }
}
