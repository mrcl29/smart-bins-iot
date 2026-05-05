package com.iot.domain;

/**
 * Represents a location within the road network using a segment ID and kilometric point.
 */
public class RoadLocation {
    private String roadSegmentId;
    private double kilometricPoint;

    public RoadLocation() {}

    public RoadLocation(String roadSegmentId, double kilometricPoint) {
        this.roadSegmentId = roadSegmentId;
        this.kilometricPoint = kilometricPoint;
    }

    public String getRoadSegmentId() { return roadSegmentId; }
    public void setRoadSegmentId(String roadSegmentId) { this.roadSegmentId = roadSegmentId; }

    public double getKilometricPoint() { return kilometricPoint; }
    public void setKilometricPoint(double kilometricPoint) { this.kilometricPoint = kilometricPoint; }

    @Override
    public String toString() {
        return "(" + roadSegmentId + ", " + kilometricPoint + ")";
    }
}
