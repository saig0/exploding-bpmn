package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import io.zeebe.client.ZeebeClient;
import java.util.List;
import java.util.Map;
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
          public void newGameStarted(List<String> playerNames) {
            LOG.debug("New game with players: {}", playerNames);
          }

          @Override
          public void handCardsDealt(Map<String, List<Card>> handCards) {
            LOG.debug("Hand cards dealt: {}", handCards);
          }

          @Override
          public void nextPlayerSelected(String player, int turns) {
            LOG.debug("Next player: {} for {} turn(s)", player, turns);
          }

          @Override
          public void cardsPlayed(String player, List<Card> cardsToPlay) {
            LOG.debug("Player {} selected {} to play.", player, cardsToPlay);
          }

          @Override
          public void playerPassed(String player) {
            LOG.debug("Player {} passed its turn.", player);
          }

          @Override
          public void playerDrawnCard(String player, Card card) {
            LOG.debug("Player {} drawn card {}.", player, card);
          }

          @Override
          public void turnEnded(String player, int remainingTurns) {
            LOG.debug("Player {} turn ended. Remaining turns {}.", player, remainingTurns);
          }
        };

    final var application = new GamesApplication(zeebeClient, gameListener);
    application.start();

    LOG.info("Ready!");
  }
}
