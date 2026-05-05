package com.iot.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload for traffic events as described in the Smart Traffic documentation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrafficMessagePayload {
    private String action;
    private String road;
    @JsonProperty("road-segment")
    private String roadSegment;
    @JsonProperty("vehicle-id")
    private String vehicleId;
    private double position;
    private String role;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getRoad() { return road; }
    public void setRoad(String road) { this.road = road; }

    public String getRoadSegment() { return roadSegment; }
    public void setRoadSegment(String roadSegment) { this.roadSegment = roadSegment; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public double getPosition() { return position; }
    public void setPosition(double position) { this.position = position; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
