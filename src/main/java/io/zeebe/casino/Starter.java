package io.zeebe.casino;

import io.zeebe.client.ZeebeClient;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Starter {

  private static final Logger LOG = LoggerFactory.getLogger(Starter.class);

  public static void main(String[] args) {

    final var zeebeClient =
        ZeebeClient.newClientBuilder()
            .brokerContactPoint("192.168.21.185:26500")
            .usePlaintext()
            .build();

    // ---
    LOG.info("> start demo");

    IntStream.range(0, 1)
        .forEach(
            i -> {
              zeebeClient
                  .newCreateInstanceCommand()
                  .bpmnProcessId("exploding-kittens")
                  .latestVersion()
                  .variables(Map.of("playerNames", List.of("phil", "chris", "basti", "niccola")))
                  .send()
                  .join();
            });
  }
}
