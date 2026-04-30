// src/tests/SensorDeviceTest.java
package tests;

import core.Message;
import core.MessagePublisher;
import domain.SensorDevice;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the SensorDevice domain logic.
 */
public class SensorDeviceTest {

    /**
     * A simple mock publisher to capture messages during tests
     * without hitting a real network.
     */
    class MockPublisher implements MessagePublisher {
        public Message lastMessage;

        @Override
        public void publish(Message message) {
            this.lastMessage = message;
        }
    }

    @Test
    public void testSendTelemetryConstructsCorrectPayload() {
        // Arrange
        MockPublisher mockPublisher = new MockPublisher();
        SensorDevice device = new SensorDevice("test-device-1", mockPublisher);

        // Act
        device.sendTelemetry(15.5);

        // Assert
        assertNotNull(mockPublisher.lastMessage, "A message should have been published.");
        assertEquals("telemetry/test-device-1", mockPublisher.lastMessage.getTopic());
        assertEquals("{\"deviceId\":\"test-device-1\", \"value\":15.50}", mockPublisher.lastMessage.getPayload());
    }
}
