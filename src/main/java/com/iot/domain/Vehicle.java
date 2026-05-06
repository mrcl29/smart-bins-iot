package com.iot.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.ports.out.MessagePublisher;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a vehicle in the Smart Traffic system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle {
    public static final String TOPIC_BASE = "es/upv/pros/tatami/smartcities/traffic/PTPaterna";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String id;
    private Characterization characterization;
    @JsonProperty("cruiser-speed")
    private int cruiserSpeed;
    private int speed;
    private String status;
    private Navigator navigator;

    @JsonIgnore
    protected MessagePublisher smartTrafficPublisher;
    @JsonIgnore
    protected RoadLocation currentLocation;

    public Vehicle() {}

    public Vehicle(String id, MessagePublisher smartTrafficPublisher) {
        this.id = id;
        this.smartTrafficPublisher = smartTrafficPublisher;
    }

    public void updateLocation(RoadLocation newLocation) {
        if (currentLocation != null && !currentLocation.getRoadSegmentId().equals(newLocation.getRoadSegmentId())) {
            publishTrafficEvent("VEHICLE_OUT", currentLocation.getRoadSegmentId(), currentLocation.getKilometricPoint());
        }
        
        boolean isNewSegment = currentLocation == null || !currentLocation.getRoadSegmentId().equals(newLocation.getRoadSegmentId());
        this.currentLocation = newLocation;
        
        if (isNewSegment) {
            publishTrafficEvent("VEHICLE_IN", currentLocation.getRoadSegmentId(), currentLocation.getKilometricPoint());
        }
    }

    public void reportAccident(String segmentId, double kp) {
        publishAlert("TRAFFIC_ACCIDENT", segmentId, kp);
    }

    protected void publishTrafficEvent(String action, String segmentId, double kp) {
        if (smartTrafficPublisher == null) return;
        try {
            Map<String, Object> msgPayload = new HashMap<>();
            msgPayload.put("action", action);
            msgPayload.put("vehicle-id", id);
            msgPayload.put("road", segmentId.split("S")[0]); // Example: R1S1 -> R1
            msgPayload.put("road-segment", segmentId);
            msgPayload.put("position", (int)kp);
            if (characterization != null) {
                msgPayload.put("role", characterization.getRole());
            }

            String jsonPayload = buildPayload("TRAFFIC", msgPayload);
            String topic = TOPIC_BASE + "/road/" + segmentId + "/traffic";
            
            smartTrafficPublisher.publish(new Message(topic, jsonPayload));
            System.out.println("[Traffic] " + action + " sent for " + id + " to " + topic);
        } catch (Exception e) {
            System.err.println("Error publishing traffic event: " + e.getMessage());
        }
    }

    protected void publishAlert(String incidentType, String segmentId, double kp) {
        if (smartTrafficPublisher == null) return;
        try {
            Map<String, Object> msgPayload = new HashMap<>();
            msgPayload.put("rt", "traffic::alert");
            msgPayload.put("incident-type", incidentType);
            msgPayload.put("id", "INC_" + System.currentTimeMillis());
            msgPayload.put("road", segmentId.split("S")[0]);
            msgPayload.put("road-segment", segmentId);
            msgPayload.put("starting-position", (int)kp);
            msgPayload.put("ending-position", (int)kp);
            msgPayload.put("description", "Vehicle Incident reported by " + id);
            msgPayload.put("status", "Active");
            msgPayload.put("link", "/incident/" + msgPayload.get("id"));

            String jsonPayload = buildPayload("ROAD_INCIDENT", msgPayload);
            String topic = TOPIC_BASE + "/road/" + segmentId + "/alerts";
            
            smartTrafficPublisher.publish(new Message(topic, jsonPayload));
            System.out.println("[Traffic] ALERT (" + incidentType + ") sent for " + id + " to " + topic);
        } catch (Exception e) {
            System.err.println("Error publishing alert: " + e.getMessage());
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Characterization {
        private String role;
        private String type;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Navigator {
        private String id;
        private String status;
        @JsonProperty("current-position")
        private String currentPosition;
        private String destination;
        @JsonProperty("remaining-distance")
        private int remainingDistance;
        private String route;
        @JsonProperty("off-road")
        private boolean offRoad;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCurrentPosition() { return currentPosition; }
        public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public int getRemainingDistance() { return remainingDistance; }
        public void setRemainingDistance(int remainingDistance) { this.remainingDistance = remainingDistance; }
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        public boolean isOffRoad() { return offRoad; }
        public void setOffRoad(boolean offRoad) { this.offRoad = offRoad; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Characterization getCharacterization() { return characterization; }
    public void setCharacterization(Characterization characterization) { this.characterization = characterization; }

    public int getCruiserSpeed() { return cruiserSpeed; }
    public void setCruiserSpeed(int cruiserSpeed) { this.cruiserSpeed = cruiserSpeed; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Navigator getNavigator() { return navigator; }
    public void setNavigator(Navigator navigator) { this.navigator = navigator; }

    @JsonIgnore
    public RoadLocation getCurrentLocation() { return currentLocation; }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", speed=" + speed +
                '}';
    }
}
