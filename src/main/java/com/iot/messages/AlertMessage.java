package com.iot.messages;

public class AlertMessage {
    public String vehicleId;
    public String type; // accidente, atasco, etc.
    public String severity;
    public long timestamp;
}
