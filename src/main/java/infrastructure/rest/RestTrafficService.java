package infrastructure.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import core.TrafficService;
import domain.Road;
import domain.RoadSegment;
import domain.Vehicle;

import java.util.List;

/**
 * REST implementation of the TrafficService.
 * Connects to the Smart Traffic server via HTTP.
 */
public class RestTrafficService implements TrafficService {
    private final HttpRestClient restClient;

    /**
     * Constructs the REST service with a base URL.
     * @param baseUrl The base URL of the Smart Traffic server (e.g., "http://ttmi008.iot.upv.es:8182").
     */
    public RestTrafficService(String baseUrl) {
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
