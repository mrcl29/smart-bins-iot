package infrastructure.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * A generic REST client for performing HTTP operations.
 */
public class HttpRestClient {
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpRestClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    public <T> T get(String path, Class<T> responseType) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + (path.startsWith("/") ? path : "/" + path)))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return deserialize(response.body(), responseType);
    }

    public <T> List<T> getList(String path, TypeReference<List<T>> typeReference) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + (path.startsWith("/") ? path : "/" + path)))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return deserializeList(response.body(), typeReference);
    }

    public <T, R> R post(String path, T requestBody, Class<R> responseType) throws Exception {
        String jsonPayload = objectMapper.writeValueAsString(requestBody);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + (path.startsWith("/") ? path : "/" + path)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return deserialize(response.body(), responseType);
    }

    private <T> T deserialize(String body, Class<T> type) throws Exception {
        if (body == null || body.isBlank() || body.equals("null")) {
            return null;
        }
        return objectMapper.readValue(body, type);
    }

    private <T> List<T> deserializeList(String body, TypeReference<List<T>> typeReference) throws Exception {
        if (body == null || body.isBlank() || body.equals("null")) {
            return List.of();
        }
        return objectMapper.readValue(body, typeReference);
    }
}
