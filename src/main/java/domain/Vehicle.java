package domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a vehicle in the Smart Traffic system.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Vehicle {
    private String id;
    private Characterization characterization;
    @JsonProperty("cruiser-speed")
    private int cruiserSpeed;
    private int speed;
    private String status;
    private Navigator navigator;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Characterization {
        private String role;
        private String type;

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Navigator {
        private String id;
        private String status;
        @JsonProperty("current-position")
        private String currentPosition;
        private String destination;
        @JsonProperty("remaining-distance")
        private int remainingDistance;
        private String route;
        @JsonProperty("off-road")
        private boolean offRoad;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCurrentPosition() { return currentPosition; }
        public void setCurrentPosition(String currentPosition) { this.currentPosition = currentPosition; }
        public String getDestination() { return destination; }
        public void setDestination(String destination) { this.destination = destination; }
        public int getRemainingDistance() { return remainingDistance; }
        public void setRemainingDistance(int remainingDistance) { this.remainingDistance = remainingDistance; }
        public String getRoute() { return route; }
        public void setRoute(String route) { this.route = route; }
        public boolean isOffRoad() { return offRoad; }
        public void setOffRoad(boolean offRoad) { this.offRoad = offRoad; }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Characterization getCharacterization() { return characterization; }
    public void setCharacterization(Characterization characterization) { this.characterization = characterization; }

    public int getCruiserSpeed() { return cruiserSpeed; }
    public void setCruiserSpeed(int cruiserSpeed) { this.cruiserSpeed = cruiserSpeed; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Navigator getNavigator() { return navigator; }
    public void setNavigator(Navigator navigator) { this.navigator = navigator; }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", speed=" + speed +
                '}';
    }
}
