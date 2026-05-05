package com.iot.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of segments (tramos) that define a path for a vehicle.
 * Matches the format: {"route": [ [point1, point2], [point3, point4] ]}
 */
public class Route {
    @JsonProperty("route")
    private List<List<RoutePoint>> segments = new ArrayList<>();

    public Route() {}

    public List<List<RoutePoint>> getSegments() { return segments; }
    public void setSegments(List<List<RoutePoint>> segments) { this.segments = segments; }

    /**
     * Adds a segment (tramo) to the route.
     * @param start The starting point of the segment.
     * @param end The ending point of the segment.
     */
    public void addSegment(RoutePoint start, RoutePoint end) {
        List<RoutePoint> tramo = new ArrayList<>();
        tramo.add(start);
        tramo.add(end);
        segments.add(tramo);
    }

    public static class RoutePoint {
        private String road;
        private double point;

        public RoutePoint() {}

        public RoutePoint(String road, double point) {
            this.road = road;
            this.point = point;
        }

        public String getRoad() { return road; }
        public void setRoad(String road) { this.road = road; }

        public double getPoint() { return point; }
        public void setPoint(double point) { this.point = point; }
    }
}
