package com.iot.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.MessageSubscriber;

import java.util.HashMap;
import java.util.Map;

/**
 * Scalable Smart Bin device implementation.
 * Uses Jackson for serialization and decoupled domain models.
 */
public class SmartBinDevice extends SensorDevice {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final double alertThreshold;
    private final String wasteType;
    private RoadLocation location;
    private final MessageSubscriber smartTrafficSubscriber;

    public SmartBinDevice(String deviceId, MessagePublisher publisher, MessageSubscriber smartTrafficSubscriber,
            double alertThreshold, String wasteType, RoadLocation location) {
        super(deviceId, publisher);
        this.smartTrafficSubscriber = smartTrafficSubscriber;
        this.alertThreshold = alertThreshold;
        this.wasteType = wasteType;
        this.location = location;

        // Notify presence on startup
        publishPresence("BIN_IN");
        setupSubscriptions();
    }

    private void setupSubscriptions() {
        if (smartTrafficSubscriber == null || location == null)
            return;
        try {
            // Bins listen to road info to make movement decisions (simulated)
            String topic = "road/" + location.getRoadSegmentId() + "/info";
            smartTrafficSubscriber.subscribe(topic, (msg) -> {
                System.out.println("[Bin-Traffic] " + getDeviceId() + " received road info: " + msg.getPayload());
            });
        } catch (Exception e) {
            System.err.println("Error setting up bin subscriptions: " + e.getMessage());
        }
    }

    /**
     * Updates the fill level and sends detailed telemetry to 'bins/sensors'.
     */
    public void updateFillLevel(double level) {
        try {

            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", getDeviceId());
            payload.put("road", location.getRoadSegmentId());
            payload.put("kp", location.getKilometricPoint());
            payload.put("level", level + "%");
            payload.put("toClean", level >= alertThreshold);
            payload.put("type", wasteType);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            Message msg = new Message("bins/sensors", jsonPayload);
            getPublisher().publish(msg);

            System.out.println("[AWS] Status sent to bins/sensors for " + getDeviceId());
        } catch (Exception e) {
            System.err.println("Error publishing bin status: " + e.getMessage());
        }
    }

    public void updateLocation(RoadLocation newLocation) {
        publishPresence("BIN_OUT");
        this.location = newLocation;
        publishPresence("BIN_IN");
    }

    private void publishPresence(String action) {
        if (location == null)
            return;
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("deviceId", getDeviceId());
            payload.put("action", action);
            payload.put("type", wasteType);
            payload.put("kp", location.getKilometricPoint());
            payload.put("road", location.getRoadSegmentId());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            String topic = "road/" + location.getRoadSegmentId() + "/bins";

            Message msg = new Message(topic, jsonPayload);
            getPublisher().publish(msg);

            System.out.println("[AWS] Presence (" + action + ") sent to " + topic);
        } catch (Exception e) {
            System.err.println("Error publishing bin presence: " + e.getMessage());
        }
    }

    public RoadLocation getRoadLocation() {
        return location;
    }
}
