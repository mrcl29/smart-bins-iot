package com.iot.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.ports.out.MessagePublisher;

/**
 * Represents a garbage truck, which is a specialized vehicle with waste collection capabilities.
 */
public class GarbageTruck extends Vehicle {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private RoadLocation currentLocation;
    private final MessagePublisher publisher;

    public GarbageTruck(String vehicleId, MessagePublisher publisher) {
        super();
        this.setId(vehicleId);
        this.publisher = publisher;
        
        // Initialize with default characterization for a garbage truck
        Characterization characterization = new Characterization();
        characterization.setRole("MedicalAssistance"); // Role used for simulation priority
        characterization.setType("GarbageTruck");
        this.setCharacterization(characterization);
    }

    public void updateLocation(RoadLocation newLocation) {
        this.currentLocation = newLocation;
        // When location is updated, we sync with the traffic system
        publishTrafficEvent("CHECK_IN");
    }

    /**
     * Simulates the waste collection action.
     */
    public void collectWaste(String binId) {
        System.out.println("Truck " + getId() + " is collecting waste from bin: " + binId);
        // This could trigger a specific message in a real scenario
    }

    private void publishTrafficEvent(String action) {
        if (currentLocation == null) return;
        
        try {
            TrafficMessagePayload payload = new TrafficMessagePayload();
            payload.setAction(action);
            payload.setVehicleId(getId());
            payload.setRoadSegment(currentLocation.getRoadSegmentId());
            payload.setPosition(currentLocation.getKilometricPoint());
            payload.setRole(getCharacterization().getRole());

            String jsonPayload = objectMapper.writeValueAsString(payload);
            String topic = "iot/2023/smart-bins/road/" + currentLocation.getRoadSegmentId() + "/traffic";
            
            Message msg = new Message(topic, jsonPayload);
            publisher.publish(msg);
        } catch (Exception e) {
            System.err.println("Error publishing traffic event for truck " + getId() + ": " + e.getMessage());
        }
    }

    public RoadLocation getCurrentLocation() { return currentLocation; }
}
