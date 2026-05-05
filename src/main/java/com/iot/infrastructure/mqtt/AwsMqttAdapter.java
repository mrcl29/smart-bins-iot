package com.iot.infrastructure.mqtt;

import com.iot.domain.Message;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * AWS IoT specific implementation of the MQTT adapter.
 */
public class AwsMqttAdapter extends AbstractMqttAdapter {
    private final String certificatePath;
    private final String privateKeyPath;
    private final String caPath;
    private MqttClientConnection connection;

    public AwsMqttAdapter(String endpoint, String certPath, String keyPath, String caPath) {
        super(endpoint, "smart-traffic-adapter-" + System.currentTimeMillis());
        this.certificatePath = certPath;
        this.privateKeyPath = keyPath;
        this.caPath = caPath;
        this.authenticateAndConnect();
    }

    private void authenticateAndConnect() {
        try (AwsIotMqttConnectionBuilder builder = AwsIotMqttConnectionBuilder.newMtlsBuilderFromPath(
                certificatePath, privateKeyPath)) {
            
            if (caPath != null && !caPath.isEmpty()) {
                builder.withCertificateAuthorityFromPath(null, caPath);
            }
            
            builder.withEndpoint(brokerUrl)
                   .withClientId(clientId)
                   .withCleanSession(true);

            this.connection = builder.build();
            CompletableFuture<Boolean> connected = connection.connect();
            
            if (connected.get()) {
                System.out.println("Successfully connected to AWS IoT Core: " + this.brokerUrl);
            }
        } catch (Exception e) {
            System.err.println("Error during AWS IoT connection: " + e.getMessage());
        }
    }

    @Override
    protected void ensureConnected() throws Exception {
        if (connection == null) {
            throw new IllegalStateException("AWS MQTT connection is not established.");
        }
    }

    @Override
    protected void performPublish(Message message) throws Exception {
        MqttMessage mqttMessage = new MqttMessage(
                message.getTopic(),
                message.getPayload().getBytes(StandardCharsets.UTF_8),
                QualityOfService.AT_LEAST_ONCE
        );
        connection.publish(mqttMessage).get();
    }

    @Override
    protected void performSubscribe(String topic, Consumer<Message> callback) throws Exception {
        connection.subscribe(topic, QualityOfService.AT_LEAST_ONCE, (mqttMessage) -> {
            String payload = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
            callback.accept(new Message(mqttMessage.getTopic(), payload));
        }).get();
    }

    @Override
    protected void performUnsubscribe(String topic) throws Exception {
        connection.unsubscribe(topic).get();
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.disconnect();
            connection.close();
        }
    }
}
