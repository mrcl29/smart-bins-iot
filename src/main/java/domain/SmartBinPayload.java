package domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data model for Smart Bin telemetry and alerts.
 * Designed for JSON serialization.
 */
public class SmartBinPayload {
    private final String deviceId;
    private final String status;
    private final double fillLevel;
    private final double threshold;
    private final BinMetadata metadata;

    public SmartBinPayload(String deviceId, String status, double fillLevel, double threshold, BinMetadata metadata) {
        this.deviceId = deviceId;
        this.status = status;
        this.fillLevel = fillLevel;
        this.threshold = threshold;
        this.metadata = metadata;
    }

    @JsonProperty("deviceId")
    public String getDeviceId() { return deviceId; }

    @JsonProperty("status")
    public String getStatus() { return status; }

    @JsonProperty("fillLevel")
    public double getFillLevel() { return fillLevel; }

    @JsonProperty("threshold")
    public double getThreshold() { return threshold; }

    @JsonProperty("metadata")
    public BinMetadata getMetadata() { return metadata; }

    public static class BinMetadata {
        private final String wasteType;
        private final Location location;

        public BinMetadata(String wasteType, Location location) {
            this.wasteType = wasteType;
            this.location = location;
        }

        @JsonProperty("wasteType")
        public String getWasteType() { return wasteType; }

        @JsonProperty("location")
        public Location getLocation() { return location; }
    }
}
