package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a segment of a road in the Smart Traffic system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadSegment {
    private String rt;
    @JsonProperty("end-kp")
    private int endKp;
    private String code;
    private double density;
    private String link;
    private int length;
    @JsonProperty("max-speed")
    private int maxSpeed;
    @JsonProperty("road-segment")
    private String roadSegment;
    @JsonProperty("start-kp")
    private int startKp;
    private int capacity;
    @JsonProperty("current-max-speed")
    private int currentMaxSpeed;
    private String road;
    @JsonProperty("num-vehicles")
    private int numVehicles;
    private String status;

    // Getters and Setters
    public String getRt() { return rt; }
    public void setRt(String rt) { this.rt = rt; }

    public int getEndKp() { return endKp; }
    public void setEndKp(int endKp) { this.endKp = endKp; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDensity() { return density; }
    public void setDensity(double density) { this.density = density; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public int getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }

    public String getRoadSegment() { return roadSegment; }
    public void setRoadSegment(String roadSegment) { this.roadSegment = roadSegment; }

    public int getStartKp() { return startKp; }
    public void setStartKp(int startKp) { this.startKp = startKp; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getCurrentMaxSpeed() { return currentMaxSpeed; }
    public void setCurrentMaxSpeed(int currentMaxSpeed) { this.currentMaxSpeed = currentMaxSpeed; }

    public String getRoad() { return road; }
    public void setRoad(String road) { this.road = road; }

    public int getNumVehicles() { return numVehicles; }
    public void setNumVehicles(int numVehicles) { this.numVehicles = numVehicles; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "RoadSegment{" +
                "code='" + code + '\'' +
                ", road='" + road + '\'' +
                ", status='" + status + '\'' +
                ", numVehicles=" + numVehicles +
                '}';
    }
}
