package producer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Set;

public class Main {

    private static final String API_URL =
            "https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones";

    private static final Set<String> BANKS =
            Set.of("BANRURAL", "GYT", "BAC", "BI");

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

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            
            for (String bank : BANKS) {
                channel.queueDeclare(bank, true, false, false, null);
            }

            System.out.println("Producer iniciado...");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.out.println("Error al obtener transacciones de la API");
                return;
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode transacciones = root.get("transacciones");

            if (transacciones == null || !transacciones.isArray()) {
                System.out.println("No se encontraron transacciones.");
                return;
            }

            for (JsonNode tx : transacciones) {

                String banco = tx.get("bancoDestino").asText();

                if (!BANKS.contains(banco)) {
                    continue;
                }

                byte[] payload =
                        mapper.writeValueAsString(tx)
                                .getBytes(StandardCharsets.UTF_8);

                
                channel.basicPublish(
                        "",
                        banco,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        payload
                );

                System.out.println("Transacción enviada a cola: " + banco);
            }

            System.out.println("Todas las transacciones fueron enviadas.");

        }
    }
}