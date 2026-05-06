package com.iot.application;

import com.iot.domain.RoadLocation;
import com.iot.domain.SmartBinDevice;
import com.iot.domain.Vehicle;
import com.iot.domain.GarbageTruck;
import com.iot.infrastructure.mqtt.AwsMqttAdapter;
import com.iot.infrastructure.mqtt.GenericMqttPublisher;
import com.iot.infrastructure.rest.MockTrafficServer;
import com.iot.infrastructure.rest.RestTrafficAdapter;
import com.iot.infrastructure.rest.RestVehicleNavigationAdapter;
import com.iot.ports.out.TrafficService;
import com.iot.ports.out.VehicleNavigationPort;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Arrays;

public class App {
    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();

        String awsEndpoint = dotenv.get("AWS_IOT_ENDPOINT");
        String certPath = dotenv.get("AWS_CERT_PATH");
        String keyPath = dotenv.get("AWS_KEY_PATH");
        String caPath = dotenv.get("AWS_CA_PATH");
        String trafficApiUrl = dotenv.get("TRAFFIC_API_URL", "http://ttmi008.iot.upv.es:10082");
        String trafficMqttHost = dotenv.get("TRAFFIC_MQTT_HOST", "ttmi008.iot.upv.es");

        MockTrafficServer mockServer = null;

        // Try to connect to the real server, fallback to mock if unreachable
        try {
            TrafficService testService = new RestTrafficAdapter(trafficApiUrl);
            testService.getSegment("R1s1");
            System.out.println("Connected to remote Traffic Server: " + trafficApiUrl);
        } catch (Exception e) {
            System.err.println("Remote server unreachable. Starting local Mock Server...");
            int mockPort = 8182;
            mockServer = new MockTrafficServer(mockPort);
            try {
                mockServer.start();
                trafficApiUrl = "http://localhost:" + mockPort;
            } catch (Exception ioException) {
                System.err.println("Failed to start Mock Server: " + ioException.getMessage());
            }
        }

        // Initialize Adapters for both brokers
        try (AwsMqttAdapter awsAdapter = new AwsMqttAdapter(awsEndpoint, certPath, keyPath, caPath);
                GenericMqttPublisher trafficMqtt = new GenericMqttPublisher(trafficMqttHost, "truck-01-client")) {

            TrafficService trafficService = new RestTrafficAdapter(trafficApiUrl);
            VehicleNavigationPort navigationPort = new RestVehicleNavigationAdapter(trafficApiUrl);

            System.out.println("\n--- Initializing Devices ---");

            // 1. Smart Bin (AWS focus)
            RoadLocation binLoc = new RoadLocation("R1s1", 10.0);
            SmartBinDevice bin1 = new SmartBinDevice("Bin_01", awsAdapter, trafficMqtt, 80.0, "ORGANIC", binLoc);

            // 2. Garbage Truck (Dual focus)
            GarbageTruck truck = new GarbageTruck("Truck_01", trafficMqtt, awsAdapter, trafficMqtt);

            System.out.println("Registering truck in Smart Traffic system...");
            navigationPort.registerVehicle(truck);

            // 3. Regular Vehicle (Traffic broker focus)
            Vehicle car = new Vehicle("Car_01", trafficMqtt);
            navigationPort.registerVehicle(car);

            System.out.println("\n--- Simulating Interactions ---");

            // Truck enters segment R1s1
            truck.updateLocation(new RoadLocation("R1s1", 0.0));
            truck.subscribeToRoad("R1s1");

            // Car reports an accident
            car.updateLocation(new RoadLocation("R1s1", 50.0));
            car.reportAccident("R1s1", 55.0);

            // Bin reaches high level -> Publishes to AWS bins/sensors
            System.out.println("\n--- Waste Collection Trigger ---");
            bin1.updateFillLevel(95.0);

            // Truck "hears" it via subscription to AWS and routes
            RouteCollectionUseCase routeUseCase = new RouteCollectionUseCase(trafficService, navigationPort,
                    trafficMqtt);
            routeUseCase.calculateAndSetRoute(truck.getId(), Arrays.asList(bin1));

            // Simulating movement of bin (if it moved)
            bin1.updateLocation(new RoadLocation("R1s2", 5.0));

            Thread.sleep(2000); // Allow some time for async messages (simulated)

            System.out.println("\n--- Simulation Finished ---");

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
