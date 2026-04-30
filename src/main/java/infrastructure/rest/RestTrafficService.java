package infrastructure.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.TrafficService;
import domain.Road;
import domain.RoadSegment;
import domain.Vehicle;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * REST implementation of the TrafficService.
 * Connects to the Smart Traffic server via HTTP.
 */
public class RestTrafficService implements TrafficService {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructs the REST service with a base URL.
     * @param baseUrl The base URL of the Smart Traffic server (e.g., "http://ttmi008.iot.upv.es:8182").
     */
    public RestTrafficService(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<Road> getAllRoads() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/roads"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), new TypeReference<List<Road>>() {});
    }

    @Override
    public Road getRoad(String roadId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/road/" + roadId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (body == null || body.isBlank()) {
            return null;
        }
        return objectMapper.readValue(body, Road.class);
    }

    @Override
    public RoadSegment getSegment(String segmentId) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/segment/" + segmentId))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        String body = response.body();
        if (body == null || body.isBlank()) {
            return null;
        }
        return objectMapper.readValue(body, RoadSegment.class);
    }

    @Override
    public List<Vehicle> getVehicles() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/vehicles"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return parseList(response.body(), new TypeReference<List<Vehicle>>() {});
    }

    /**
     * Helper method to parse a JSON list, handling empty or blank responses.
     */
    private <T> List<T> parseList(String body, TypeReference<List<T>> typeReference) throws Exception {
        if (body == null || body.isBlank() || body.equals("null")) {
            return List.of();
        }
        return objectMapper.readValue(body, typeReference);
    }
}
