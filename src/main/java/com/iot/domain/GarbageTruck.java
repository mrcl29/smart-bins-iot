package com.iot.domain;

import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.MessageSubscriber;

/**
 * Represents a garbage truck, which is a specialized vehicle with waste collection capabilities.
 */
public class GarbageTruck extends Vehicle {
    private final MessageSubscriber awsSubscriber;
    private final MessageSubscriber smartTrafficSubscriber;

    public GarbageTruck(String vehicleId, MessagePublisher smartTrafficPublisher,
                                MessageSubscriber awsSubscriber, MessageSubscriber smartTrafficSubscriber) {
        super(vehicleId, smartTrafficPublisher);
        this.awsSubscriber = awsSubscriber;
        this.smartTrafficSubscriber = smartTrafficSubscriber;

        // Initialize characterization
        Characterization charact = new Characterization();
        charact.setRole("MedicalAssistance"); // Priority role
        charact.setType("GarbageTruck");
        this.setCharacterization(charact);

        setupSubscriptions();
    }

    private void setupSubscriptions() {
        try {
            // Subscribe to AWS Bins topics
            awsSubscriber.subscribe(Vehicle.TOPIC_BASE + "/bins/sensors", (msg) -> {
                System.out.println("[Truck-AWS] Received bin status update: " + msg.getPayload());
            });

            // Subscribe to Smart Traffic Road Info
            // In a real scenario, this would be per road segment the truck is interested in
            // For simulation, we can use a wildcard if supported or specific ones
        } catch (Exception e) {
            System.err.println("Error setting up truck subscriptions: " + e.getMessage());
        }
    }

    public void subscribeToRoad(String segmentId) {
        try {
            // AWS: Bin locations in this road
            awsSubscriber.subscribe(Vehicle.TOPIC_BASE + "/road/" + segmentId + "/bins", (msg) -> {
                System.out.println("[Truck-AWS] Bin presence on " + segmentId + ": " + msg.getPayload());
            });

            // Smart Traffic: Road status/events
            smartTrafficSubscriber.subscribe(Vehicle.TOPIC_BASE + "/road/" + segmentId + "/info", (msg) -> {
                System.out.println("[Truck-Traffic] Road info update for " + segmentId + ": " + msg.getPayload());
            });
        } catch (Exception e) {
            System.err.println("Error subscribing to road " + segmentId + ": " + e.getMessage());
        }
    }

    /**
     * Simulates the waste collection action.
     */
    public void collectWaste(String binId) {
        System.out.println("Truck " + getId() + " is collecting waste from bin: " + binId);
    }
}
