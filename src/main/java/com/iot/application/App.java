package com.iot.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iot.domain.RoadLocation;
import com.iot.domain.SmartBinDevice;
import com.iot.domain.Vehicle;
import com.iot.domain.GarbageTruck;
import com.iot.domain.Message;
import com.iot.infrastructure.mqtt.AwsMqttAdapter;
import com.iot.infrastructure.mqtt.PahoMqttAdapter;
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

/**
 * Main application class that orchestrates the Smart Traffic simulation
 * using real AWS and Smart Traffic brokers.
 */
public class App {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        // Load configuration from .env
        String awsEndpoint = dotenv.get("AWS_IOT_ENDPOINT");
        String certPath = dotenv.get("AWS_CERT_PATH");
        String keyPath = dotenv.get("AWS_KEY_PATH");
        String caPath = dotenv.get("AWS_CA_PATH");

        // Smart Traffic Infrastructure
        String trafficApiUrl = dotenv.get("TRAFFIC_API_URL", "http://ttmi008.iot.upv.es:8182");
        String trafficMqttHost = dotenv.get("TRAFFIC_MQTT_HOST", "tcp://tambori.dsic.upv.es:10083");

        System.out.println("Initializing Adapters with real Infrastructure...");
        System.out.println("Connecting to Smart Traffic REST API: " + trafficApiUrl);

        // Initialize Adapters for both real brokers
        try (AwsMqttAdapter awsAdapter = new AwsMqttAdapter(awsEndpoint, certPath, keyPath, caPath);
                PahoMqttAdapter trafficMqtt = new PahoMqttAdapter(trafficMqttHost,
                        "smart-traffic-app-" + System.currentTimeMillis())) {

            TrafficService trafficService = new RestTrafficAdapter(trafficApiUrl);
            VehicleNavigationPort navigationPort = new RestVehicleNavigationAdapter(trafficApiUrl);

            try {
                System.out.println("Testing REST connectivity...");
                trafficService.getVehicles();
                System.out.println("REST API is reachable.");
            } catch (Exception e) {
                System.err.println("\n[ERROR] Unable to reach Smart Traffic REST API at " + trafficApiUrl);
                System.err.println("[HINT] Ensure you are connected to the UPV VPN and the server is online.");
                System.err.println("[DETAIL] " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            // --- STABILIZATION DELAY ---
            // Give AWS and Traffic brokers time to settle the handshake
            System.out.println("Waiting for connections to stabilize...");
            Thread.sleep(3000);

            System.out.println("\n--- [SIMULATION STARTING ON REAL SERVERS] ---");

            // --- PHASE 1: Active Road Agent logic ---
            // Listens to accidents and propagates them to the info topic
            trafficMqtt.subscribe(Vehicle.TOPIC_BASE + "/road/+/alerts", (msg) -> {
                try {
                    String topic = msg.getTopic();
                    String segment = topic.split("/")[topic.split("/").length - 2];

                    System.out.println("[Road-Agent] Incident on " + segment + ". Updating info...");

                    Map<String, Object> incidentMsg = new HashMap<>();
                    incidentMsg.put("rt", "traffic::incident");
                    incidentMsg.put("incident-type", "INCIDENT");
                    incidentMsg.put("id", "ROAD_INC_" + System.currentTimeMillis());
                    incidentMsg.put("road", segment.split("S")[0]);
                    incidentMsg.put("road-segment", segment);
                    incidentMsg.put("description", "Accident detected");
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
            Vehicle car1 = new Vehicle("Car_1", trafficMqtt, trafficMqtt);
            Vehicle car2 = new Vehicle("Car_2", trafficMqtt, trafficMqtt);
            GarbageTruck truck1 = new GarbageTruck("Truck_1", trafficMqtt, awsAdapter, trafficMqtt);
            GarbageTruck truck2 = new GarbageTruck("Truck_2", trafficMqtt, awsAdapter, trafficMqtt);

            navigationPort.registerVehicle(car1);
            navigationPort.registerVehicle(car2);
            navigationPort.registerVehicle(truck1);
            navigationPort.registerVehicle(truck2);

            car1.updateLocation(new RoadLocation("R1S1", 100));
            car2.updateLocation(new RoadLocation("R2S1", 200));
            truck1.updateLocation(new RoadLocation("R3S1", 50));
            truck2.updateLocation(new RoadLocation("R1S1", 300));

            System.out.println("\n2. Initializing 9 Smart Bins across 3 roads...");
            List<SmartBinDevice> bins = new ArrayList<>();
            String[] types = {"ORGANIC", "PLASTIC", "GLASS"};
            String[] roads = {"R1S1", "R2S1", "R3S1"};

            for (int i = 0; i < 9; i++) {
                String road = roads[i % 3];
                String type = types[i / 3];
                bins.add(new SmartBinDevice("Bin_" + (i + 1), awsAdapter, trafficMqtt, 80.0, type,
                        new RoadLocation(road, 50 * (i + 1))));
            }

            // --- PHASE 3: Simulation Events ---
            System.out.println("\n3. Simulating traffic flow and fill levels...");
            car1.updateLocation(new RoadLocation("R2S1", 150));
            bins.get(0).updateFillLevel(95.0); // Bin_1 (ORGANIC, R1S1)
            bins.get(4).updateFillLevel(98.0); // Bin_5 (PLASTIC, R2S1)

            System.out.println("\n4. Triggering an accident on R3S1...");
            car2.reportAccident("R3S1", 200);

            // Wait for Bins to react to the real MQTT message and migrate
            Thread.sleep(3000);

            System.out.println("\n5. Truck_1 coordinating collection...");
            RouteCollectionUseCase routeUseCase = new RouteCollectionUseCase(trafficService, navigationPort,
                    trafficMqtt);
            List<SmartBinDevice> fullBins = Arrays.asList(bins.get(0), bins.get(4));
            routeUseCase.calculateAndSetRoute(truck1.getId(), fullBins);

            for (SmartBinDevice bin : fullBins) {
                truck1.updateLocation(bin.getRoadLocation());
                truck1.collectWaste(bin.getDeviceId());
                bin.updateFillLevel(0.0);
            }

            System.out.println("\n--- [SIMULATION COMPLETED ON REAL SERVERS] ---");

        } catch (Exception e) {
            System.err.println("Fatal Application Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
