package com.iot.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a sequence of segments that define a path for a vehicle.
 */
public class Route {
    private List<RouteSegment> segments = new ArrayList<>();

    public Route() {}

    public List<RouteSegment> getSegments() { return segments; }
    public void setSegments(List<RouteSegment> segments) { this.segments = segments; }

    public static class RouteSegment {
        private String road;
        private double point;

        public RouteSegment() {}

        public RouteSegment(String road, double point) {
            this.road = road;
            this.point = point;
        }

        public String getRoad() { return road; }
        public void setRoad(String road) { this.road = road; }

        public double getPoint() { return point; }
        public void setPoint(double point) { this.point = point; }
    }
}
