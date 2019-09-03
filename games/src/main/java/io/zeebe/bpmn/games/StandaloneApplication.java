package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.GameState;
import io.zeebe.bpmn.games.model.Player;
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

    final var gameListener =
        new GameListener() {

          @Override
          public void newGameStarted(GameState state) {
            LOG.debug("New game: {}", state);
          }

          @Override
          public void nextPlayerSelected(String player, int turns) {
            LOG.debug("Next player: {} for {} turn(s)", player, turns);
          }
        };

    final var application = new GamesApplication(zeebeClient, gameListener);
    application.start();

    LOG.info("Ready!");
  }
}
