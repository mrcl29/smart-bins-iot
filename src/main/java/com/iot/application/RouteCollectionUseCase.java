package com.iot.application;

import com.iot.domain.GarbageTruck;
import com.iot.domain.Route;
import com.iot.domain.SmartBinDevice;
import com.iot.domain.TrafficMessagePayload;
import com.iot.ports.out.MessageSubscriber;
import com.iot.ports.out.TrafficService;
import com.iot.ports.out.VehicleNavigationPort;
import java.util.List;

/**
 * Use case for collecting waste from smart bins.
 */
public class RouteCollectionUseCase {
    private final TrafficService trafficService;
    private final VehicleNavigationPort navigationPort;
    private final MessageSubscriber subscriber;

    public RouteCollectionUseCase(TrafficService trafficService, 
                                  VehicleNavigationPort navigationPort, 
                                  MessageSubscriber subscriber) {
        this.trafficService = trafficService;
        this.navigationPort = navigationPort;
        this.subscriber = subscriber;
    }

    /**
     * Simulates the calculation and setting of a collection route.
     * In this project, route calculation is theoretical and therefore simulated.
     */
    public void calculateAndSetRoute(String truckId, List<SmartBinDevice> bins) {
        System.out.println("[SIMULATION] Calculating optimal collection route for truck: " + truckId);
        System.out.println("[SIMULATION] Analyzing fill levels for " + bins.size() + " bins...");
        
        // Theoretical Route: In a real implementation, this would involve pathfinding
        // based on bin locations and current traffic density.
        Route simulatedRoute = new Route();
        for (SmartBinDevice bin : bins) {
            // A segment (tramo) is a list of two points
            Route.RoutePoint start = new Route.RoutePoint(bin.getRoadLocation().getRoadSegmentId(), 0.0);
            Route.RoutePoint end = new Route.RoutePoint(bin.getRoadLocation().getRoadSegmentId(), bin.getRoadLocation().getKilometricPoint());
            simulatedRoute.addSegment(start, end);
        }

        System.out.println("[SIMULATION] Route optimized. Total stops: " + simulatedRoute.getSegments().size());

        try {
            // We still send the "calculated" route to the navigation port to simulate the message exchange
            navigationPort.setRoute(truckId, simulatedRoute);
            System.out.println("[SIMULATION] Route message sent to truck " + truckId);
        } catch (Exception e) {
            System.err.println("Error simulating route for truck " + truckId + ": " + e.getMessage());
        }
    }

    public void handleTrafficIncident(TrafficMessagePayload incident) {
        System.out.println("Handling traffic incident at " + incident.getRoadSegment() + ": " + incident.getAction());
        // Logic to reroute trucks if necessary
    }
}
