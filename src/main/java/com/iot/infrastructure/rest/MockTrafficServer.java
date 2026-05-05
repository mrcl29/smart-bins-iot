package com.iot.infrastructure.rest;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * A simple simulated server that mocks the Smart Traffic REST API.
 * Used as a fallback when the real server is unreachable.
 */
public class MockTrafficServer {
    private HttpServer server;
    private final int port;

    public MockTrafficServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Endpoint: GET /roads
        server.createContext("/roads", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "[{\"rt\":\"road\",\"id\":\"R1\",\"name\":\"Simulated Road 1\",\"segments\":[]}]";
                sendResponse(exchange, response, 200);
            }
        });

        // Endpoint: POST /vehicles
        server.createContext("/vehicles", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                System.out.println("[MOCK SERVER] Received registration for vehicle");
                sendResponse(exchange, "{\"status\":\"Registered\"}", 201);
            }
        });

        // Endpoint: PUT /vehicle/
        server.createContext("/vehicle/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                System.out.println("[MOCK SERVER] Received route update for vehicle: " + exchange.getRequestURI());
                sendResponse(exchange, "{\"status\":\"Route Set\"}", 200);
            }
        });

        // Endpoint: GET /segment/
        server.createContext("/segment/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String response = "{\"rt\":\"road-segment\",\"status\":\"Free_Flow\",\"num-vehicles\":0}";
                sendResponse(exchange, response, 200);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("[MOCK SERVER] Started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
