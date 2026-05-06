package com.iot.tests;

import com.iot.domain.Message;
import com.iot.ports.out.MessagePublisher;
import com.iot.domain.RoadLocation;
import com.iot.domain.SmartBinDevice;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

    class MockSubscriber implements com.iot.ports.out.MessageSubscriber {
        @Override
        public void subscribe(String topic, java.util.function.Consumer<Message> callback) {}
        @Override
        public void unsubscribe(String topic) {}
    }

    @Test
    public void testUpdateFillLevelPublishesToCorrectTopic() {
        MockPublisher mockPublisher = new MockPublisher();
        MockSubscriber mockSubscriber = new MockSubscriber();
        RoadLocation loc = new RoadLocation("R1S1", 10.0);
        SmartBinDevice bin = new SmartBinDevice("bin-1", mockPublisher, mockSubscriber, 80.0, "ORGANIC", loc);

        // Constructor already sends 1 BIN_IN message
        assertEquals(1, mockPublisher.publishedMessages.size());
        assertTrue(mockPublisher.publishedMessages.get(0).getTopic().contains("R1S1/bins"));
        assertTrue(mockPublisher.publishedMessages.get(0).getTopic().startsWith(SmartBinDevice.TOPIC_BASE));

        bin.updateFillLevel(50.0);

        // Should have 2 messages now: 1 BIN_IN and 1 sensor status
        assertEquals(2, mockPublisher.publishedMessages.size());
        assertTrue(mockPublisher.publishedMessages.get(1).getTopic().contains("bins/sensors"));
        assertTrue(mockPublisher.publishedMessages.get(1).getPayload().contains("\"level\":50"));
        assertTrue(mockPublisher.publishedMessages.get(1).getPayload().contains("\"toClean\":false"));
    }

    @Test
    public void testUpdateLocationPublishesOutAndIn() {
        MockPublisher mockPublisher = new MockPublisher();
        MockSubscriber mockSubscriber = new MockSubscriber();
        RoadLocation loc1 = new RoadLocation("R1S1", 10.0);
        SmartBinDevice bin = new SmartBinDevice("bin-1", mockPublisher, mockSubscriber, 80.0, "ORGANIC", loc1);

        bin.updateLocation(new RoadLocation("R1S2", 5.0));

        // 1 (Initial BIN_IN) + 1 (BIN_OUT) + 1 (New BIN_IN)
        assertEquals(3, mockPublisher.publishedMessages.size());
        assertTrue(mockPublisher.publishedMessages.get(1).getPayload().contains("\"action\":\"BIN_OUT\""));
        assertTrue(mockPublisher.publishedMessages.get(1).getTopic().contains("R1S1/bins"));
        
        assertTrue(mockPublisher.publishedMessages.get(2).getPayload().contains("\"action\":\"BIN_IN\""));
        assertTrue(mockPublisher.publishedMessages.get(2).getTopic().contains("R1S2/bins"));
    }
}
