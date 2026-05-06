package com.iot.application;

import com.iot.domain.Route;
import com.iot.domain.SmartBinDevice;
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
     */
    public void calculateAndSetRoute(String truckId, List<SmartBinDevice> bins) {
        System.out.println("[SIMULATION] Calculating optimal route for truck: " + truckId);
        
        Route simulatedRoute = new Route();
        for (SmartBinDevice bin : bins) {
            Route.RoutePoint start = new Route.RoutePoint(bin.getRoadLocation().getRoadSegmentId(), 0.0);
            Route.RoutePoint end = new Route.RoutePoint(bin.getRoadLocation().getRoadSegmentId(), bin.getRoadLocation().getKilometricPoint());
            simulatedRoute.addSegment(start, end);
        }

        try {
            navigationPort.setRoute(truckId, simulatedRoute);
            System.out.println("[SIMULATION] Route optimized and sent to navigation system.");
        } catch (Exception e) {
            System.err.println("Error setting route: " + e.getMessage());
        }
    }
}
