package io.zeebe.bpmn.games;

import com.google.common.base.Strings;
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
            LOG.debug("Player {}'s turn ended. Remaining turns {}.", player, remainingTurns);
          }

          @Override
          public void cardsDiscarded(String player, List<Card> cards) {
            LOG.debug("Player {} put the cards {} onto the discard pile.", player, cards);
          }

          @Override
          public void playerToDrawSelected(String player, String playerToDrawFrom) {
            LOG.debug(
                "Player {} selected player {} to draw a card from.", player, playerToDrawFrom);
          }

          @Override
          public void cardTakenFrom(String player, String playerTakenFrom, Card card) {
            LOG.debug("Player {} took the card {} from player {}.", player, card, playerTakenFrom);
          }

          @Override
          public void cardChosenFrom(String player, String playerChosenFrom, Card card) {
            LOG.debug(
                "Player {} choose the card {} from player {}.", player, card, playerChosenFrom);
          }

          @Override
          public void playerSawTheFuture(String player, List<Card> cards) {
            LOG.debug("Player {} saw the future {}.", player, cards);
          }

          @Override
          public void deckShuffled(List<Card> deck) {
            LOG.debug("Deck was shuffled {}.", deck);
          }

          @Override
          public void playerAlteredTheFuture(String player, List<Card> cards) {
            LOG.debug("Player {} altered the future {}.", player, cards);
          }

          @Override
          public void deckReordered(List<Card> deck) {
            LOG.debug("Deck was reordered {}.", deck);
          }

          @Override
          public void handCheckedForDefuse(String player, List<Card> hand) {
            LOG.debug("Checked player {}'s hand {} for a defuse card.", player, hand);
          }

          @Override
          public void playerInsertedCard(String player, Card card, List<Card> deck) {
            LOG.debug("Player {} inserted the card {} into the deck {}.", player, card, deck);
          }

          @Override
          public void playerExploded(String player) {
            LOG.debug("Player {} exploded.", player);
          }

          @Override
          public void playerWonTheGame(String player) {
            final int count = 19 - player.length();
            LOG.debug("\n"
                + "=========================================="
                + "\n============= WINNER: {} " + Strings.repeat("=", count > 0 ? count : 0)
                + "\n==========================================", player);
          }
        };

    final var application = new GamesApplication(zeebeClient, gameListener);
    application.start();

    LOG.info("Ready!");
  }
}
