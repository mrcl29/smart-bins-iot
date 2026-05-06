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
    public static final String TOPIC_BASE = "es/upv/pros/tatami/smartcities/traffic/PTPaterna";

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
            String topic = TOPIC_BASE + "/road/" + location.getRoadSegmentId() + "/info";
            smartTrafficSubscriber.subscribe(topic, (msg) -> {
                System.out.println("[Bin-Traffic] " + getDeviceId() + " received road info: " + msg.getPayload());
                // Reaction to accident: move to another road
                if (msg.getPayload().contains("ROAD_INCIDENT") || msg.getPayload().contains("ACCIDENT")) {
                    System.out.println("[Bin-Action] " + getDeviceId() + " detected incident! Moving to safety.");
                    String currentSegment = location.getRoadSegmentId();
                    String nextSegment = currentSegment.equals("R3S1") ? "R1S1" : (currentSegment.equals("R1S1") ? "R2S1" : "R1S1");
                    updateLocation(new RoadLocation(nextSegment, location.getKilometricPoint()));
                }
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
            Map<String, Object> msgPayload = new HashMap<>();
            msgPayload.put("deviceId", getDeviceId());
            msgPayload.put("road", location.getRoadSegmentId().split("S")[0]);
            msgPayload.put("road-segment", location.getRoadSegmentId());
            msgPayload.put("kp", (int)location.getKilometricPoint());
            msgPayload.put("level", (int)level);
            msgPayload.put("toClean", level >= alertThreshold);
            msgPayload.put("type", wasteType);

            String jsonPayload = buildPayload("BIN_SENSOR", msgPayload);
            Message msg = new Message(TOPIC_BASE + "/bins/sensors", jsonPayload);
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
        // Re-setup subscriptions for the new road segment if necessary
        // In a real system we would unsubscribe from old and subscribe to new
    }

    private void publishPresence(String action) {
        if (location == null)
            return;
        try {
            Map<String, Object> msgPayload = new HashMap<>();
            msgPayload.put("deviceId", getDeviceId());
            msgPayload.put("action", action);
            msgPayload.put("type", wasteType);
            msgPayload.put("kp", (int)location.getKilometricPoint());
            msgPayload.put("road", location.getRoadSegmentId().split("S")[0]);
            msgPayload.put("road-segment", location.getRoadSegmentId());

            String jsonPayload = buildPayload("BIN_POSITION", msgPayload);
            String topic = TOPIC_BASE + "/road/" + location.getRoadSegmentId() + "/bins";

            Message msg = new Message(topic, jsonPayload);
            getPublisher().publish(msg);

            System.out.println("[AWS] Presence (" + action + ") sent to " + topic);
        } catch (Exception e) {
            System.err.println("Error publishing bin presence: " + e.getMessage());
        }
    }

    private String buildPayload(String type, Map<String, Object> msgPayload) throws Exception {
        Map<String, Object> root = new HashMap<>();
        long ts = System.currentTimeMillis();
        root.put("id", "MSG_" + ts);
        root.put("type", type);
        root.put("timestamp", ts);
        root.put("msg", msgPayload);
        return objectMapper.writeValueAsString(root);
    }

    public RoadLocation getRoadLocation() {
        return location;
    }
}
