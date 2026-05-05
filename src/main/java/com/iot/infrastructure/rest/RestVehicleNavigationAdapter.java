package com.iot.infrastructure.rest;

import com.iot.domain.Route;
import com.iot.domain.Vehicle;
import com.iot.ports.out.VehicleNavigationPort;
import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for vehicle navigation using REST.
 */
public class RestVehicleNavigationAdapter implements VehicleNavigationPort {
    private final HttpRestClient restClient;

    public RestVehicleNavigationAdapter(String baseUrl) {
        this.restClient = new HttpRestClient(baseUrl);
    }

    @Override
    public void registerVehicle(Vehicle vehicle) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", vehicle.getId());
        payload.put("speed", vehicle.getSpeed() > 0 ? vehicle.getSpeed() : 80);
        
        if (vehicle.getCharacterization() != null) {
            payload.put("type", vehicle.getCharacterization().getType());
            payload.put("role", vehicle.getCharacterization().getRole());
        } else {
            payload.put("type", "Automobile");
            payload.put("role", "PrivateUsage");
        }
        
        // According to PDF 7.1.7: POST /vehicles to create a new vehicle
        restClient.post("/vehicles", payload, Object.class);
        System.out.println("Vehicle registered successfully: " + vehicle.getId());
    }

    @Override
    public void setRoute(String vehicleId, Route route) throws Exception {
        // According to PDF 7.1.8: PUT /vehicle/{vehicleId} with route payload
        restClient.put("/vehicle/" + vehicleId, route, Object.class);
    }
}
