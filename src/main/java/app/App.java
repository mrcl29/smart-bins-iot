// src/App.java
package app;

import core.MessagePublisher;
import domain.SensorDevice;
import infrastructure.aws.AwsIotPublisher;
import infrastructure.mqtt.GenericMqttPublisher;

public class App {
    public static void main(String[] args) {
        // For local testing, you might use Mosquitto
        // MessagePublisher publisher = new GenericMqttPublisher("ssl://localhost:8883",
        // "client-001");

        // For production, inject the AWS IoT implementation
        MessagePublisher awsPublisher = new AwsIotPublisher(
                System.getenv("AWS_IOT_ENDPOINT"),
                System.getenv("AWS_CERT_PATH"),
                System.getenv("AWS_KEY_PATH"));

        // The device logic remains identical regardless of the publisher
        SensorDevice temperatureSensor = new SensorDevice("temp-sensor-valencia-01", awsPublisher);

        // Simulate reading
        temperatureSensor.sendTelemetry(24.5);
    }
}
