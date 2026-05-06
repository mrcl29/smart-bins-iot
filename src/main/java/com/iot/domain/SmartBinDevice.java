package com.iot.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.domain.Message;
import com.iot.ports.out.MessagePublisher;
import com.iot.messages.BinSensorMessage;
import com.iot.utils.JsonUtil;

/**
 * Scalable Smart Bin device implementation.
 * Uses Jackson for serialization and decoupled domain models.
 */
public class SmartBinDevice extends SensorDevice {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final double alertThreshold;
    private final String wasteType;
    private RoadLocation location;

    public SmartBinDevice(String deviceId, MessagePublisher publisher, double alertThreshold,
            String wasteType, RoadLocation location) {
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

    public void updateLocation(RoadLocation newLocation) {
        this.location = newLocation;
    }

    public RoadLocation getRoadLocation() {
        return location;
    }

    private void publishMessage(double level, String status, String topicPrefix) {
        try {
            // Crear mensaje según nuevo modelo
            BinSensorMessage msg = new BinSensorMessage();
            msg.binId = getDeviceId();
            msg.fillLevel = (int) level;
            msg.type = wasteType;
            msg.timestamp = System.currentTimeMillis();
            msg.roadSegment = location != null ? location.toString() : "unknown";

            // Convertir a JSON
            String jsonPayload = JsonUtil.toJson(msg);

            // Topic correcto según arquitectura
            String topic = "bins/sensors";

            Message message = new Message(topic, jsonPayload);
            getPublisher().publish(message);

            System.out.println("SmartBin JSON sent: " + jsonPayload);

        } catch (Exception e) {
            System.err.println("Error publishing smart bin message: " + e.getMessage());
        }
    }
}
