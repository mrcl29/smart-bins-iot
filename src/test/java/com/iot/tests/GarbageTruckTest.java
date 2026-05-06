package com.iot.tests;

import com.iot.domain.GarbageTruck;
import com.iot.domain.Message;
import com.iot.domain.RoadLocation;
import com.iot.ports.out.MessagePublisher;
import com.iot.ports.out.MessageSubscriber;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GarbageTruckTest {

    class MockPublisher implements MessagePublisher {
        public List<Message> publishedMessages = new ArrayList<>();

        @Override
        public void publish(Message message) {
            this.publishedMessages.add(message);
        }
    }

    class MockSubscriber implements MessageSubscriber {
        @Override
        public void subscribe(String topic, Consumer<Message> callback) {}
        @Override
        public void unsubscribe(String topic) {}
    }

    @Test
    public void testGarbageTruckInitialization() {
        MockPublisher mockTrafficPublisher = new MockPublisher();
        MockSubscriber mockAwsSubscriber = new MockSubscriber();
        MockSubscriber mockTrafficSubscriber = new MockSubscriber();
        
        GarbageTruck truck = new GarbageTruck("Truck-1", mockTrafficPublisher, mockAwsSubscriber, mockTrafficSubscriber);

        assertEquals("Truck-1", truck.getId());
        assertEquals("MedicalAssistance", truck.getCharacterization().getRole());
        assertEquals("GarbageTruck", truck.getCharacterization().getType());
    }

    @Test
    public void testUpdateLocationPublishesTrafficEvent() {
        MockPublisher mockTrafficPublisher = new MockPublisher();
        MockSubscriber mockAwsSubscriber = new MockSubscriber();
        MockSubscriber mockTrafficSubscriber = new MockSubscriber();
        
        GarbageTruck truck = new GarbageTruck("Truck-1", mockTrafficPublisher, mockAwsSubscriber, mockTrafficSubscriber);
        RoadLocation loc = new RoadLocation("R1S1", 0.0);

        truck.updateLocation(loc);

        // Should have 1 VEHICLE_IN message
        assertEquals(1, mockTrafficPublisher.publishedMessages.size());
        Message msg = mockTrafficPublisher.publishedMessages.get(0);
        assertTrue(msg.getTopic().contains(GarbageTruck.TOPIC_BASE + "/road/R1S1/traffic"));
        assertTrue(msg.getPayload().contains("\"action\":\"VEHICLE_IN\""));
    }
}
