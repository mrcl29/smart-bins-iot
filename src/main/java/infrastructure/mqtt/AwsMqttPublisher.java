package infrastructure.mqtt;

import core.Message;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttMessage;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.iot.AwsIotMqttConnectionBuilder;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * AWS IoT specific implementation using AWS IoT Device SDK v2.
 */
public class AwsMqttPublisher extends AbstractMqttPublisher {
    private final String certificatePath;
    private final String privateKeyPath;
    private final String caPath;
    private MqttClientConnection connection;

    public AwsMqttPublisher(String endpoint, String certPath, String keyPath, String caPath) {
        super(endpoint, "smart-traffic-publisher-" + System.currentTimeMillis());
        this.certificatePath = certPath;
        this.privateKeyPath = keyPath;
        this.caPath = caPath;
        this.authenticateAndConnect();
    }

    private void authenticateAndConnect() {
        System.out.println("Connecting to AWS IoT with:");
        System.out.println(" - Endpoint: " + brokerUrl);
        System.out.println(" - Cert: " + certificatePath);
        System.out.println(" - Key: " + privateKeyPath);
        System.out.println(" - CA: " + caPath);

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
            } else {
                System.err.println("Failed to connect to AWS IoT Core.");
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error during AWS IoT connection: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Unexpected error connecting to AWS IoT: " + e.getMessage());
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

        CompletableFuture<Integer> published = connection.publish(mqttMessage);
        published.get(); // Wait for the publish to complete
    }

    @Override
    public void close() {
        if (connection != null) {
            connection.disconnect();
            connection.close();
        }
    }
}
