package com.iot.tests;

import com.iot.domain.GarbageTruck;
import com.iot.domain.Message;
import com.iot.domain.RoadLocation;
import com.iot.ports.out.MessagePublisher;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class GarbageTruckTest {

    class MockPublisher implements MessagePublisher {
        public List<Message> publishedMessages = new ArrayList<>();

        @Override
        public void publish(Message message) {
            this.publishedMessages.add(message);
        }
    }

    @Test
    public void testGarbageTruckInitialization() {
        MockPublisher mockPublisher = new MockPublisher();
        GarbageTruck truck = new GarbageTruck("Truck-1", mockPublisher);

        assertEquals("Truck-1", truck.getId());
        assertEquals("MedicalAssistance", truck.getCharacterization().getRole());
        assertEquals("GarbageTruck", truck.getCharacterization().getType());
    }

    @Test
    public void testUpdateLocationPublishesEvent() {
        MockPublisher mockPublisher = new MockPublisher();
        GarbageTruck truck = new GarbageTruck("Truck-1", mockPublisher);
        RoadLocation loc = new RoadLocation("R5S1", 0.0);

        truck.updateLocation(loc);

        assertEquals(1, mockPublisher.publishedMessages.size());
        Message msg = mockPublisher.publishedMessages.get(0);
        assertTrue(msg.getTopic().contains("R5S1/traffic"));
        assertTrue(msg.getPayload().contains("\"action\":\"CHECK_IN\""));
        assertTrue(msg.getPayload().contains("\"vehicle-id\":\"Truck-1\""));
    }
}
