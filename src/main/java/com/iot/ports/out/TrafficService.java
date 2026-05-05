package com.iot.ports.out;

import com.iot.domain.Road;
import com.iot.domain.RoadSegment;
import com.iot.domain.Vehicle;
import java.util.List;

/**
 * Interface defining the services available to interact with the road network.
 */
public interface TrafficService {
    /**
     * Obtains information about all available roads.
     * @return A list of all roads.
     */
    List<Road> getAllRoads() throws Exception;

    /**
     * Obtains information about a specific road.
     * @param roadId The identifier of the road (e.g., "R10").
     * @return The road information.
     */
    Road getRoad(String roadId) throws Exception;

    /**
     * Obtains information about a specific road segment.
     * @param segmentId The identifier of the segment (e.g., "R5s1").
     * @return The segment information.
     */
    RoadSegment getSegment(String segmentId) throws Exception;

    /**
     * Obtains the simulated vehicles currently running.
     * @return A list of vehicles.
     */
    List<Vehicle> getVehicles() throws Exception;
}
