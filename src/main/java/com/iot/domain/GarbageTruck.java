package com.iot.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.ports.out.MessagePublisher;

/**
 * Represents a garbage truck that interacts with the smart traffic system.
 */
public class GarbageTruck {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final String vehicleId;
    private RoadLocation currentLocation;
    private final MessagePublisher publisher;

    public GarbageTruck(String vehicleId, MessagePublisher publisher) {
        this.vehicleId = vehicleId;
        this.publisher = publisher;
    }

    public void updateLocation(RoadLocation newLocation) {
        this.currentLocation = newLocation;
        publishTrafficEvent("CHECK_IN");
    }

    private void publishTrafficEvent(String action) {
        try {
            TrafficMessagePayload payload = new TrafficMessagePayload();
            payload.setAction(action);
            payload.setVehicleId(vehicleId);
            payload.setRoadSegment(currentLocation.getRoadSegmentId());
            payload.setPosition(currentLocation.getKilometricPoint());
            payload.setRole("MedicalAssistance"); // Using a role compatible with the simulation examples

            String jsonPayload = objectMapper.writeValueAsString(payload);
            // According to PDF, vehicles publish to topic: <topic-base>/road/{id-segmento}/traffic
            String topic = "iot/2023/smart-bins/road/" + currentLocation.getRoadSegmentId() + "/traffic";
            
            Message msg = new Message(topic, jsonPayload);
            publisher.publish(msg);
        } catch (Exception e) {
            System.err.println("Error publishing traffic event for truck " + vehicleId + ": " + e.getMessage());
        }
    }

    public String getVehicleId() { return vehicleId; }
    public RoadLocation getCurrentLocation() { return currentLocation; }
}
