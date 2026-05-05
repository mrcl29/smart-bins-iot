package com.iot.application;

import com.iot.domain.RoadLocation;
import com.iot.domain.Road;
import com.iot.domain.RoadSegment;
import com.iot.domain.SensorDevice;
import com.iot.domain.SmartBinDevice;
import com.iot.domain.Vehicle;
import com.iot.domain.GarbageTruck;
import com.iot.infrastructure.mqtt.AwsMqttAdapter;
import com.iot.infrastructure.rest.RestTrafficAdapter;
import com.iot.infrastructure.rest.RestVehicleNavigationAdapter;
import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.MessageSubscriber;
import com.iot.ports.out.TrafficService;
import com.iot.ports.out.VehicleNavigationPort;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String awsEndpoint = dotenv.get("AWS_IOT_ENDPOINT");
        String certPath = dotenv.get("AWS_CERT_PATH");
        String keyPath = dotenv.get("AWS_KEY_PATH");
        String caPath = dotenv.get("AWS_CA_PATH");
        String trafficApiUrl = "http://ttmi008.iot.upv.es:8182";

        try (AwsMqttAdapter mqttAdapter = new AwsMqttAdapter(awsEndpoint, certPath, keyPath, caPath)) {
            
            TrafficService trafficService = new RestTrafficAdapter(trafficApiUrl);
            VehicleNavigationPort navigationPort = new RestVehicleNavigationAdapter(trafficApiUrl);
            
            // Domain entities
            System.out.println("\n--- Initializing Devices ---");
            RoadLocation bin1Loc = new RoadLocation("R1S1", 10.0);
            SmartBinDevice bin1 = new SmartBinDevice("Bin_Valencia_Org_01", mqttAdapter, 80.0, "ORGANIC", bin1Loc);

            RoadLocation truckLoc = new RoadLocation("R5s1", 0.0);
            GarbageTruck truck = new GarbageTruck("Truck_01", mqttAdapter);
            truck.updateLocation(truckLoc);

            // Use Case orchestration
            RouteCollectionUseCase routeUseCase = new RouteCollectionUseCase(trafficService, navigationPort, mqttAdapter);
            
            System.out.println("\n--- Simulating Waste Collection ---");
            bin1.updateFillLevel(95.0); // Triggers alert
            
            routeUseCase.calculateAndSetRoute(truck.getVehicleId(), Arrays.asList(bin1));

            // Traffic monitoring simulation
            System.out.println("\n--- Traffic Monitoring ---");
            try {
                List<Road> roads = trafficService.getAllRoads();
                System.out.println("Available roads: " + roads.size());
                
                RoadSegment segment = trafficService.getSegment("R1S1");
                System.out.println("R1S1 Status: " + segment.getStatus());
            } catch (Exception e) {
                System.err.println("Traffic API error: " + e.getMessage());
            }

            // Subscribe to traffic alerts (as per PDF Paso 1)
            mqttAdapter.subscribe("iot/2023/smart-bins/road/R1S1/alerts", (msg) -> {
                System.out.println("ALERT RECEIVED: " + msg.getPayload());
            });

        } catch (Exception e) {
            System.err.println("Application Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
