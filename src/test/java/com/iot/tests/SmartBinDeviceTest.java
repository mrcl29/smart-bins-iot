package com.iot.tests;

import com.iot.domain.Message;
import com.iot.ports.out.MessagePublisher;
import com.iot.domain.RoadLocation;
import com.iot.domain.SmartBinDevice;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

public class SmartBinDeviceTest {

    class MockPublisher implements MessagePublisher {
        public List<Message> publishedMessages = new ArrayList<>();

        @Override
        public void publish(Message message) {
            this.publishedMessages.add(message);
        }
    }

    @Test
    public void testUpdateFillLevelBelowThreshold() {
        MockPublisher mockPublisher = new MockPublisher();
        RoadLocation loc = new RoadLocation("R1S1", 10.0);
        SmartBinDevice bin = new SmartBinDevice("bin-1", mockPublisher, 80.0, "ORGANIC", loc);

        bin.updateFillLevel(50.0);

        assertEquals(1, mockPublisher.publishedMessages.size());
        assertEquals("telemetry/bins/bin-1", mockPublisher.publishedMessages.get(0).getTopic());
    }

    @Test
    public void testUpdateFillLevelAboveThresholdSendsAlert() {
        MockPublisher mockPublisher = new MockPublisher();
        RoadLocation loc = new RoadLocation("R2S1", 50.0);
        SmartBinDevice bin = new SmartBinDevice("bin-1", mockPublisher, 80.0, "PLASTIC", loc);

        bin.updateFillLevel(85.0);

        // Should send 2 messages: telemetry and alert
        assertEquals(2, mockPublisher.publishedMessages.size());

        boolean hasTelemetry = mockPublisher.publishedMessages.stream()
                .anyMatch(m -> m.getTopic().equals("telemetry/bins/bin-1"));
        boolean hasAlert = mockPublisher.publishedMessages.stream()
                .anyMatch(m -> m.getTopic().equals("alerts/bins/bin-1"));

        assertTrue(hasTelemetry, "Should have telemetry message");
        assertTrue(hasAlert, "Should have alert message");

        Message alertMsg = mockPublisher.publishedMessages.stream()
                .filter(m -> m.getTopic().equals("alerts/bins/bin-1"))
                .findFirst().orElseThrow();

        assertTrue(alertMsg.getPayload().contains("\"status\":\"CRITICAL\""));
        assertTrue(alertMsg.getPayload().contains("\"wasteType\":\"PLASTIC\""));
        assertTrue(alertMsg.getPayload().contains("\"roadSegmentId\":\"R2S1\""));
    }
}
