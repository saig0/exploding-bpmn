package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackGameStateNotifier implements GameListener {

  private static final Logger LOG = LoggerFactory.getLogger(SlackGameStateNotifier.class);

  @Autowired private SlackContext slackContext;

  @Autowired private MethodsClient methodsClient;
  private List<String> players;

  private void sendPrivateMessage(String channelId, String message) {
    try {

      methodsClient.chatPostMessage(req -> req.channel(channelId).text(message));

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String formatCard(Card card) {
    return card.getType().name();
  }

  private String formatCards(List<Card> cards) {
    return cards.stream().map(this::formatCard).collect(Collectors.joining(", "));
  }

  @Override
  public void newGameStarted(List<String> playerNames) {
    this.players = playerNames;
  }

  @Override
  public void handCardsDealt(Map<String, List<Card>> handCards) {
    handCards.forEach(
        (player, cards) -> {
          final var channelId = slackContext.getUser(player).getChannelId();

          sendPrivateMessage(channelId, String.format("Your hand cards: %s", formatCards(cards)));
        });
  }

  @Override
  public void nextPlayerSelected(String player, int turns) {

    players.forEach(
        userId -> {
          final var channelId = slackContext.getUser(userId).getChannelId();

          sendPrivateMessage(channelId, String.format("Next player: %s (%d turns)", player, turns));
        });
  }

  @Override
  public void cardsPlayed(String player, List<Card> cards) {

    players.forEach(
        userId -> {
          final var channelId = slackContext.getUser(userId).getChannelId();

          sendPrivateMessage(
              channelId, String.format("Player %s played %s.", player, formatCards(cards)));
        });
  }

  @Override
  public void playerPassed(String player) {

    players.forEach(
        userId -> {
          final var channelId = slackContext.getUser(userId).getChannelId();

          sendPrivateMessage(channelId, String.format("Player %s passed.", player));
        });
  }

  @Override
  public void playerDrawnCard(String player, Card card) {

    final var channelId = slackContext.getUser(player).getChannelId();

    sendPrivateMessage(channelId, String.format("You draw the card: %s", formatCard(card)));
  }

  @Override
  public void turnEnded(String player, int remainingTurns) {}

  @Override
  public void cardsDiscarded(String player, List<Card> cards) {}

  @Override
  public void playerToDrawSelected(String player, String playerToDrawFrom) {}

  @Override
  public void cardTakenFrom(String player, String playerTakenFrom, Card card) {}

  @Override
  public void cardChosenFrom(String player, String playerChosenFrom, Card card) {}

  @Override
  public void playerSawTheFuture(String player, List<Card> cards) {}

  @Override
  public void deckShuffled(List<Card> deck) {}

  @Override
  public void playerAlteredTheFuture(String player, List<Card> cards) {}

  @Override
  public void deckReordered(List<Card> deck) {}

  @Override
  public void handCheckedForDefuse(String player, List<Card> hand) {}

  @Override
  public void playerInsertedCard(String player, Card card, List<Card> deck) {}

  @Override
  public void playerExploded(String player) {}

  @Override
  public void playerWonTheGame(String player) {}

  @Override
  public void playerNoped(String player, List<Card> nopedCards) {}
}
