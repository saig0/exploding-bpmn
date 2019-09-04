package io.zeebe.bpmn.games;

import io.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneApplication {

  private static final Logger LOG = LoggerFactory.getLogger(StandaloneApplication.class);

  public static void main(String[] args) {

    LOG.info("Connect to Zeebe");

    final var zeebeClient =
        ZeebeClient.newClientBuilder().brokerContactPoint("localhost:26500").usePlaintext().build();

    LOG.info("Launch workers");

    final var gameListener = new GameStateLogger();

    final var application = new GamesApplication(zeebeClient, gameListener);
    application.start();

    LOG.info("Ready!");
  }
}
