// src/App.java
package com.iot.application;

import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.TrafficService;
import com.iot.domain.Location;
import com.iot.domain.Road;
import com.iot.domain.RoadSegment;
import com.iot.domain.SensorDevice;
import com.iot.domain.SmartBinDevice;
import com.iot.domain.Vehicle;
import com.iot.infrastructure.mqtt.AwsMqttPublisher;
import com.iot.infrastructure.rest.RestTrafficService;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;

public class App {
    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.load();

        try (MessagePublisher awsPublisher = new AwsMqttPublisher(
                dotenv.get("AWS_IOT_ENDPOINT"),
                dotenv.get("AWS_CERT_PATH"),
                dotenv.get("AWS_KEY_PATH"),
                dotenv.get("AWS_CA_PATH"))) {

            // Generic sensor
            SensorDevice temperatureSensor = new SensorDevice("temp-sensor-valencia-01", awsPublisher);
            temperatureSensor.sendTelemetry(24.5);

            // Smart Bin implementation - Refactored for scalability
            System.out.println("\n--- Smart Bin Monitoring ---");
            
            // Bin 1: Organic waste in Valencia City Center
            Location colonLocation = new Location("Carrer de Colón", 39.4697, -0.3725);
            SmartBinDevice binOrganic = new SmartBinDevice(
                "Bin_Valencia_Org_01", awsPublisher, 80.0, "ORGANIC", colonLocation);
            
            // Bin 2: Plastic waste near the University
            Location blascoLocation = new Location("Avinguda de Blasco Ibáñez", 39.4791, -0.3468);
            SmartBinDevice binPlastic = new SmartBinDevice(
                "Bin_Valencia_Pla_05", awsPublisher, 75.0, "PLASTIC", blascoLocation);

            System.out.println("Updating Bin 1 (Organic)...");
            binOrganic.updateFillLevel(40.0);
            
            System.out.println("Updating Bin 2 (Plastic) - Triggering Alert...");
            binPlastic.updateFillLevel(92.0);

            System.out.println("\n--- Smart Traffic Integration ---");
            TrafficService trafficService = new RestTrafficService("http://ttmi008.iot.upv.es:8182");

            try {
                System.out.println("Fetching all roads...");
                List<Road> roads = trafficService.getAllRoads();
                System.out.println("Available roads: " + roads.size());
                if (!roads.isEmpty()) {
                    System.out.println("Example road: " + roads.get(0));
                }

                System.out.println("\nFetching information for road R10...");
                Road r10 = trafficService.getRoad("R10");
                System.out.println("Road R10 Name: " + r10.getName());
                if (r10.getSegments() != null && !r10.getSegments().isEmpty()) {
                    System.out.println("Road R10 First Segment Status: " + r10.getSegments().get(0).getStatus());
                }

                System.out.println("\nFetching information for segment R5s1...");
                RoadSegment r5s1 = trafficService.getSegment("R5s1");
                System.out.println("Segment R5s1 density: " + r5s1.getDensity());

                System.out.println("\nFetching simulated vehicles...");
                List<Vehicle> vehicles = trafficService.getVehicles();
                System.out.println("Active vehicles: " + vehicles.size());

            } catch (Exception e) {
                System.err.println("Error communicating with Smart Traffic server: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("Error in IoT Publisher: " + e.getMessage());
        }
    }
}
