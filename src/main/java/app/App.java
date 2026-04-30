// src/App.java
package app;

import core.MessagePublisher;
import core.TrafficService;
import domain.Road;
import domain.RoadSegment;
import domain.SensorDevice;
import domain.Vehicle;
import infrastructure.aws.AwsIotPublisher;
import infrastructure.rest.RestTrafficService;

import java.util.List;

public class App {
    public static void main(String[] args) {
        // --- Existing IoT Device Simulation ---
        MessagePublisher awsPublisher = new AwsIotPublisher(
                System.getenv("AWS_IOT_ENDPOINT"),
                System.getenv("AWS_CERT_PATH"),
                System.getenv("AWS_KEY_PATH"));

        SensorDevice temperatureSensor = new SensorDevice("temp-sensor-valencia-01", awsPublisher);
        temperatureSensor.sendTelemetry(24.5);

        // --- New Smart Traffic Integration ---
        System.out.println("\n--- Smart Traffic Integration ---");
        TrafficService trafficService = new RestTrafficService("http://ttmi008.iot.upv.es:8182");

        try {
            // 1. Get all roads
            System.out.println("Fetching all roads...");
            List<Road> roads = trafficService.getAllRoads();
            System.out.println("Available roads: " + roads.size());
            if (!roads.isEmpty()) {
                System.out.println("Example road: " + roads.get(0));
            }

            // 2. Get information about a specific road (e.g., R10)
            System.out.println("\nFetching information for road R10...");
            Road r10 = trafficService.getRoad("R10");
            System.out.println("Road R10 Name: " + r10.getName());
            if (r10.getSegments() != null && !r10.getSegments().isEmpty()) {
                System.out.println("Road R10 First Segment Status: " + r10.getSegments().get(0).getStatus());
            }

            // 3. Get information about a specific segment (e.g., R5s1)
            System.out.println("\nFetching information for segment R5s1...");
            RoadSegment r5s1 = trafficService.getSegment("R5s1");
            System.out.println("Segment R5s1 density: " + r5s1.getDensity());

            // 4. Get running simulated vehicles
            System.out.println("\nFetching simulated vehicles...");
            List<Vehicle> vehicles = trafficService.getVehicles();
            System.out.println("Active vehicles: " + vehicles.size());

        } catch (Exception e) {
            System.err.println("Error communicating with Smart Traffic server: " + e.getMessage());
            // This might happen if not connected to UPV network/VPN
        }
    }
}
