package com.iot.infrastructure.rest;

import com.iot.domain.Route;
import com.iot.ports.out.VehicleNavigationPort;

/**
 * Adapter for vehicle navigation using REST.
 */
public class RestVehicleNavigationAdapter implements VehicleNavigationPort {
    private final HttpRestClient restClient;

    public RestVehicleNavigationAdapter(String baseUrl) {
        this.restClient = new HttpRestClient(baseUrl);
    }

    @Override
    public void setRoute(String vehicleId, Route route) throws Exception {
        // According to PDF 7.1.8: PUT /vehicle/{vehicleId} with route payload
        restClient.put("/vehicle/" + vehicleId, route, Object.class);
    }
}
