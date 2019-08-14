package io.zeebe.casino;

import io.zeebe.client.ZeebeClient;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {

    final var zeebeClient =
        ZeebeClient.newClientBuilder()
            .brokerContactPoint("192.168.30.188:26500")
            .usePlaintext()
            .build();

    // ---
    LOG.info("> deploying workflows");
    zeebeClient.newDeployCommand().addResourceFromClasspath("explodingKittens.bpmn").send().join();

    // ---
    LOG.info("> start demo");
    zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("Process_1")
        .latestVersion()
        .variables(
            Map.of(
                "players",
                List.of("phil", "chris"),
                "round",
                0,
                "turns",
                1,
                "correlationKey",
                UUID.randomUUID()))
        .send()
        .join();

    zeebeClient
        .newWorker()
        .jobType("build-deck")
        .handler(new BuildDeck(LOG))
        .name("build-deck")
        .open();

    zeebeClient
        .newWorker()
        .jobType("selectPlayerForNewRound")
        .handler(new SelectPlayer(LOG))
        .name("playerSelector")
        .open();
  }
}
