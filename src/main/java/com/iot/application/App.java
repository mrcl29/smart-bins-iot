package com.iot.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.domain.RoadLocation;
import com.iot.domain.SmartBinDevice;
import com.iot.domain.Vehicle;
import com.iot.domain.GarbageTruck;
import com.iot.domain.Message;
import com.iot.infrastructure.mqtt.GenericMqttPublisher;
import com.iot.infrastructure.rest.MockTrafficServer;
import com.iot.infrastructure.rest.RestTrafficAdapter;
import com.iot.infrastructure.rest.RestVehicleNavigationAdapter;
import com.iot.ports.out.TrafficService;
import com.iot.ports.out.VehicleNavigationPort;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String awsEndpoint = dotenv.get("AWS_IOT_ENDPOINT", "simulated-aws");
        String trafficApiUrl = dotenv.get("TRAFFIC_API_URL", "http://localhost:8182");
        String trafficMqttHost = dotenv.get("TRAFFIC_MQTT_HOST", "tcp://tambori.dsic.upv.es:10083");

        MockTrafficServer mockServer = null;

        // Start mock server for navigation API
        System.out.println("Starting local Mock Server for REST API...");
        int mockPort = 8182;
        mockServer = new MockTrafficServer(mockPort);
        try {
            mockServer.start();
            trafficApiUrl = "http://localhost:" + mockPort;
        } catch (Exception ioException) {
            System.err.println("Failed to start Mock Server: " + ioException.getMessage());
        }

        // Initialize Adapters. We use GenericMqttPublisher for both to simulate communication.
        try (GenericMqttPublisher awsAdapter = new GenericMqttPublisher(awsEndpoint, "aws-client");
             GenericMqttPublisher trafficMqtt = new GenericMqttPublisher(trafficMqttHost, "traffic-client")) {

            TrafficService trafficService = new RestTrafficAdapter(trafficApiUrl);
            VehicleNavigationPort navigationPort = new RestVehicleNavigationAdapter(trafficApiUrl);

            System.out.println("\n--- [SIMULATION START] ---");

            // --- PHASE 1: Active Road Simulation Logic ---
            // Simulate Roads as active objects that propagate alerts to info topic
            trafficMqtt.subscribe(Vehicle.TOPIC_BASE + "/road/+/alerts", (msg) -> {
                try {
                    String topic = msg.getTopic();
                    String[] parts = topic.split("/");
                    String segment = parts[parts.length - 2];
                    
                    System.out.println("[Road-Agent] Incident detected on " + segment + ". Updating info topic...");
                    
                    Map<String, Object> incidentMsg = new HashMap<>();
                    incidentMsg.put("rt", "traffic::incident");
                    incidentMsg.put("incident-type", "INCIDENT");
                    incidentMsg.put("id", "ROAD_INC_" + System.currentTimeMillis());
                    incidentMsg.put("road", segment.split("S")[0]);
                    incidentMsg.put("road-segment", segment);
                    incidentMsg.put("description", "Accident confirmed on segment");
                    incidentMsg.put("status", "Active");

                    Map<String, Object> wrapper = new HashMap<>();
                    long ts = System.currentTimeMillis();
                    wrapper.put("id", "MSG_" + ts);
                    wrapper.put("type", "ROAD_INCIDENT");
                    wrapper.put("timestamp", ts);
                    wrapper.put("msg", incidentMsg);

                    trafficMqtt.publish(new Message(Vehicle.TOPIC_BASE + "/road/" + segment + "/info", 
                        new ObjectMapper().writeValueAsString(wrapper)));
                } catch (Exception e) { e.printStackTrace(); }
            });

            // --- PHASE 2: Initializing Devices ---
            System.out.println("\n1. Initializing Vehicles and Trucks...");
            Vehicle car1 = new Vehicle("Car_1", trafficMqtt);
            Vehicle car2 = new Vehicle("Car_2", trafficMqtt);
            GarbageTruck truck1 = new GarbageTruck("Truck_1", trafficMqtt, awsAdapter, trafficMqtt);
            GarbageTruck truck2 = new GarbageTruck("Truck_2", trafficMqtt, awsAdapter, trafficMqtt);

            navigationPort.registerVehicle(car1);
            navigationPort.registerVehicle(car2);
            navigationPort.registerVehicle(truck1);
            navigationPort.registerVehicle(truck2);

            // Set initial locations
            car1.updateLocation(new RoadLocation("R1S1", 100));
            car2.updateLocation(new RoadLocation("R2S1", 200));
            truck1.updateLocation(new RoadLocation("R3S1", 50));
            truck2.updateLocation(new RoadLocation("R1S1", 300));
            
            truck1.subscribeToRoad("R1S1");
            truck1.subscribeToRoad("R2S1");
            truck1.subscribeToRoad("R3S1");

            System.out.println("\n2. Initializing 9 Smart Bins across 3 roads...");
            List<SmartBinDevice> bins = new ArrayList<>();
            String[] types = {"ORGANIC", "PLASTIC", "GLASS"};
            String[] roads = {"R1S1", "R2S1", "R3S1"};

            for (int i = 0; i < 9; i++) {
                String road = roads[i % 3];
                String type = types[i / 3];
                RoadLocation loc = new RoadLocation(road, 50 * (i + 1));
                bins.add(new SmartBinDevice("Bin_" + (i + 1), awsAdapter, trafficMqtt, 80.0, type, loc));
            }

            // --- PHASE 3: Movements and Fill Levels ---
            System.out.println("\n3. Simulating vehicle movements...");
            car1.updateLocation(new RoadLocation("R2S1", 150));
            truck2.updateLocation(new RoadLocation("R3S1", 250));

            System.out.println("\n4. Bins reporting high fill levels...");
            bins.get(0).updateFillLevel(95.0); // Bin_1 (ORGANIC, R1S1)
            bins.get(4).updateFillLevel(98.0); // Bin_5 (PLASTIC, R2S1)

            // --- PHASE 4: Accident and Migration ---
            System.out.println("\n5. Reporting an accident on R3S1...");
            car2.reportAccident("R3S1", 200);

            // Wait a bit for Bins to react to the accident info
            Thread.sleep(2000);

            // --- PHASE 5: Routing and Collection ---
            System.out.println("\n6. Truck_1 calculating collection route...");
            RouteCollectionUseCase routeUseCase = new RouteCollectionUseCase(trafficService, navigationPort, trafficMqtt);
            
            // Only collect full bins (Bin_1 and Bin_5)
            List<SmartBinDevice> fullBins = Arrays.asList(bins.get(0), bins.get(4));
            routeUseCase.calculateAndSetRoute(truck1.getId(), fullBins);

            System.out.println("\n7. Performing collection...");
            for (SmartBinDevice bin : fullBins) {
                truck1.updateLocation(bin.getRoadLocation());
                truck1.collectWaste(bin.getDeviceId());
                bin.updateFillLevel(0.0); // Report bin as empty
            }

            System.out.println("\n--- [SIMULATION FINISHED] ---");

        } catch (Exception e) {
            System.err.println("Application Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (mockServer != null) {
                mockServer.stop();
            }
        }
    }
}