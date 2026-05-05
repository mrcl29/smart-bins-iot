package com.iot.ports.out;

import com.iot.domain.Route;

/**
 * Port for sending navigation commands to vehicles.
 */
public interface VehicleNavigationPort {
    /**
     * Sets a route for a specific vehicle.
     * 
     * @param vehicleId The identifier of the vehicle.
     * @param route     The route to follow.
     */
    void setRoute(String vehicleId, Route route) throws Exception;
}
