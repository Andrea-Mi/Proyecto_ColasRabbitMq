package consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Main {

    private static final Set<String> BANK_QUEUES =
            Set.of("BANRURAL", "GYT", "BAC", "BI");

    private static final String POST_URL =
            "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";

    private static final String NOMBRE =
            "Tania Andrea Miranda Ramirez";

    private static final String CARNET =
            "0905-24-16058";

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("andreagt.ramirez@gmail.com");
        factory.setPassword("Tania..123");

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ObjectMapper mapper = new ObjectMapper();

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // Procesar una transacción a la vez
        channel.basicQos(1);

        for (String queue : BANK_QUEUES) {
            channel.queueDeclare(queue, true, false, false, null);
        }

        for (String queue : BANK_QUEUES) {

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                boolean ok = process(delivery, client, mapper);

                if (ok) {

                    channel.basicAck(
                            delivery.getEnvelope().getDeliveryTag(),
                            false
                    );

                } else {

                    channel.basicNack(
                            delivery.getEnvelope().getDeliveryTag(),
                            false,
                            true
                    );
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        }

        System.out.println("=================================");
        System.out.println("   Consumer activo...");
        System.out.println("   Esperando transacciones...");
        System.out.println("=================================\n");

        new CountDownLatch(1).await();
    }

    private static boolean process(
            Delivery delivery,
            HttpClient client,
            ObjectMapper mapper
    ) {

        try {

            String payload =
                    new String(delivery.getBody(), StandardCharsets.UTF_8);

            JsonNode tx = mapper.readTree(payload);

            String banco = tx.get("bancoDestino").asText();
            double monto = tx.get("monto").asDouble();

            System.out.println("Procesando transacción del banco: " + banco);

            ObjectNode obj = (ObjectNode) tx;

            obj.put("nombre", NOMBRE);
            obj.put("carnet", CARNET);

            String json = mapper.writeValueAsString(obj);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(POST_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();

            if (status == 200 || status == 201) {

                System.out.println("==============================");
                System.out.println("Transacción procesada");
                System.out.println("Banco: " + banco);
                System.out.println("Monto: Q" + monto);
                System.out.println("Estado: Guardada correctamente");
                System.out.println("==============================\n");

                return true;

            } else {

                System.out.println("Error al guardar transacción. Código HTTP: " + status);
            }

        } catch (Exception e) {

            System.out.println("Error al procesar transacción: " + e.getMessage());
        }

        return false;
    }
}