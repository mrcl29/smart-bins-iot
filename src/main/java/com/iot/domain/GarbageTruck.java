package com.iot.domain;

import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.MessageSubscriber;

/**
 * Represents a garbage truck, which is a specialized vehicle with waste collection capabilities.
 */
public class GarbageTruck extends Vehicle {
    private final MessageSubscriber awsSubscriber;

    public GarbageTruck(String vehicleId, MessagePublisher smartTrafficPublisher,
                                MessageSubscriber awsSubscriber, MessageSubscriber smartTrafficSubscriber) {
        super(vehicleId, smartTrafficPublisher, smartTrafficSubscriber);
        this.awsSubscriber = awsSubscriber;

        // Initialize characterization
        Characterization charact = new Characterization();
        charact.setRole("MedicalAssistance"); // Priority role
        charact.setType("GarbageTruck");
        this.setCharacterization(charact);

        setupStaticSubscriptions();
    }

    private void setupStaticSubscriptions() {
        try {
            // Subscribe to AWS Bins sensors topic (Global for the truck)
            awsSubscriber.subscribe(SmartBinDevice.TOPIC_BASE_AWS + "bins/sensors", (msg) -> {
                System.out.println("[Truck-AWS] Received bin status update: " + msg.getPayload());
            });
        } catch (Exception e) {
            System.err.println("Error setting up truck static subscriptions: " + e.getMessage());
        }
    }

    @Override
    protected void handleRoadSubscribe(String segmentId) {
        super.handleRoadSubscribe(segmentId);
        try {
            // AWS: Bin locations in this road
            awsSubscriber.subscribe(SmartBinDevice.TOPIC_BASE_AWS + "road/" + segmentId + "/bins", (msg) -> {
                System.out.println("[Truck-AWS] Bin presence on " + segmentId + ": " + msg.getPayload());
            });
        } catch (Exception e) {
            System.err.println("Error subscribing to AWS road bins for " + getId() + ": " + e.getMessage());
        }
    }

    @Override
    protected void handleRoadUnsubscribe(String segmentId) {
        super.handleRoadUnsubscribe(segmentId);
        try {
            // AWS: Bin locations in this road
            awsSubscriber.unsubscribe(SmartBinDevice.TOPIC_BASE_AWS + "road/" + segmentId + "/bins");
        } catch (Exception e) {
            System.err.println("Error unsubscribing from AWS road bins for " + getId() + ": " + e.getMessage());
        }
    }

    /**
     * Simulates the waste collection action.
     */
    public void collectWaste(String binId) {
        System.out.println("Truck " + getId() + " is collecting waste from bin: " + binId);
    }
}
