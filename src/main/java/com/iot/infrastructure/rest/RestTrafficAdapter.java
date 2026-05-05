package com.iot.infrastructure.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iot.domain.Road;
import com.iot.domain.RoadSegment;
import com.iot.domain.Vehicle;
import com.iot.ports.out.TrafficService;
import java.util.List;

/**
 * Adapter for the Traffic Service using REST.
 */
public class RestTrafficAdapter implements TrafficService {
    private final HttpRestClient restClient;

    public RestTrafficAdapter(String baseUrl) {
        this.restClient = new HttpRestClient(baseUrl);
    }

    @Override
    public List<Road> getAllRoads() throws Exception {
        return restClient.getList("/roads", new TypeReference<List<Road>>() {});
    }

    @Override
    public Road getRoad(String roadId) throws Exception {
        return restClient.get("/road/" + roadId, Road.class);
    }

    @Override
    public RoadSegment getSegment(String segmentId) throws Exception {
        return restClient.get("/segment/" + segmentId, RoadSegment.class);
    }

    @Override
    public List<Vehicle> getVehicles() throws Exception {
        return restClient.getList("/vehicles", new TypeReference<List<Vehicle>>() {});
    }
}
