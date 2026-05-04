package domain;

/**
 * Represents the physical location of an IoT device.
 */
public class Location {
    private final String street;
    private final double latitude;
    private final double longitude;

    public Location(String street, double latitude, double longitude) {
        this.street = street;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStreet() { return street; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
