package com.iot.ports.out;

import com.iot.domain.Route;
import com.iot.domain.Vehicle;

/**
 * Port for sending navigation commands and managing vehicles.
 */
public interface VehicleNavigationPort {
    /**
     * Registers a new vehicle in the simulator.
     * 
     * @param vehicle The vehicle to register.
     * @throws Exception if registration fails.
     */
    void registerVehicle(Vehicle vehicle) throws Exception;

    /**
     * Sets a route for a specific vehicle.
     * 
     * @param vehicleId The identifier of the vehicle.
     * @param route     The route to follow.
     */
    void setRoute(String vehicleId, Route route) throws Exception;
}
