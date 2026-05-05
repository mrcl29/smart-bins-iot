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

    public void calculateAndSetRoute(String truckId, List<SmartBinDevice> bins) {
        System.out.println("Calculating route for truck " + truckId + " based on bin fill levels...");
        
        Route route = new Route();
        // Simplified route calculation: just go to the first bin's location
        for (SmartBinDevice bin : bins) {
            // This is a simplification. In a real scenario, we would use TrafficService to find the path.
            route.getSegments().add(new Route.RouteSegment(bin.getRoadLocation().getRoadSegmentId(), bin.getRoadLocation().getKilometricPoint()));
        }

        try {
            navigationPort.setRoute(truckId, route);
            System.out.println("Route set successfully for truck " + truckId);
        } catch (Exception e) {
            System.err.println("Error setting route for truck " + truckId + ": " + e.getMessage());
        }
    }

    public void handleTrafficIncident(TrafficMessagePayload incident) {
        System.out.println("Handling traffic incident at " + incident.getRoadSegment() + ": " + incident.getAction());
        // Logic to reroute trucks if necessary
    }
}
