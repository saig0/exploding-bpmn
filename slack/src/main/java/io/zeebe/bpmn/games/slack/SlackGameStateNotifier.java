package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.slack.SlackContext.UserInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
  private List<String> users;

  private void sendMessageTo(String channelId, String message) {
    try {

      methodsClient.chatPostMessage(req -> req.channel(channelId).text(message));

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendMessage(Function<String, String> messageForUser) {
    users.forEach(
        userId -> {
          final var channelId =
              Optional.ofNullable(slackContext.getUser(userId))
                  .map(UserInfo::getChannelId)
                  .orElseThrow(() -> new RuntimeException("unknown user"));

          final var message = messageForUser.apply(userId);

          sendMessageTo(channelId, message);
        });
  }

  private String formatCard(Card card) {
    return card.getType().name();
  }

  private String formatCards(List<Card> cards) {
    return cards.stream().map(this::formatCard).collect(Collectors.joining(", "));
  }

  private String formatPlayer(String userId) {
    return Optional.ofNullable(slackContext.getUser(userId))
        .map(UserInfo::getUserName)
        .orElse(String.format("unknown (%s)", userId));
  }

  @Override
  public void newGameStarted(List<String> playerNames) {
    this.users = playerNames;
  }

  @Override
  public void handCardsDealt(Map<String, List<Card>> handCards) {
    sendMessage(
        user -> {
          final var hand = handCards.get(user);
          return String.format("Your hand cards: %s", formatCards(hand));
        });
  }

  @Override
  public void nextPlayerSelected(String player, int turns) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format("You are next for %d turn(s)", turns);
          } else {
            return String.format("Next player: %s for %d turn(s)", formatPlayer(player), turns);
          }
        });
  }

  @Override
  public void cardsPlayed(String player, List<Card> cards) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format("You played: %s", formatCards(cards));
          } else {
            return String.format("Player %s played %s.", formatPlayer(player), formatCards(cards));
          }
        });
  }

  @Override
  public void playerPassed(String player) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return "You passed.";
          } else {
            return String.format("Player %s passed.", formatPlayer(player));
          }
        });
  }

  @Override
  public void playerDrawnCard(String player, Card card) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format("You draw the card: %s", formatCard(card));
          } else {
            return String.format("Player %s draw a card.", formatPlayer(player));
          }
        });
  }

  @Override
  public void turnEnded(String player, int remainingTurns) {}

  @Override
  public void cardsDiscarded(String player, List<Card> cards) {}

  @Override
  public void playerToDrawSelected(String player, String playerToDrawFrom) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format(
                "You chose %s to draw a card from.", formatPlayer(playerToDrawFrom));
          } else {
            return String.format(
                "Player %s chose %s to draw a card form.",
                formatPlayer(player), formatPlayer(playerToDrawFrom));
          }
        });
  }

  @Override
  public void cardTakenFrom(String player, String playerTakenFrom, Card card) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format(
                "You get the card %s from %s.", formatCard(card), formatPlayer(playerTakenFrom));
          } else {
            return String.format(
                "Player %s get a card form %s.",
                formatPlayer(player), formatPlayer(playerTakenFrom));
          }
        });
  }

  @Override
  public void cardChosenFrom(String player, String playerChosenFrom, Card card) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format(
                "You get the card %s from %s.", formatCard(card), formatPlayer(playerChosenFrom));
          } else {
            return String.format(
                "Player %s get a card form %s.",
                formatPlayer(player), formatPlayer(playerChosenFrom));
          }
        });
  }

  @Override
  public void playerSawTheFuture(String player, List<Card> cards) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format(
                "The future (the top 3 cards of the deck): %s", formatCards(cards));
          } else {
            return String.format(
                "Player %s saw the future (the top 3 cards of the deck)", formatPlayer(player));
          }
        });
  }

  @Override
  public void deckShuffled(List<Card> deck) {
    sendMessage(user -> "The deck is shuffled.");
  }

  @Override
  public void playerAlteredTheFuture(String player, List<Card> cards) {
    sendMessageTo(
        player,
        String.format(
            "Alter the future by changing the order of the top cards of the deck: %s",
            formatCards(cards)));
  }

  @Override
  public void deckReordered(List<Card> deck) {
    sendMessage(user -> "The future was altered (the order of the top 3 cards was changed)");
  }

  @Override
  public void handCheckedForDefuse(String player, List<Card> hand) {}

  @Override
  public void playerInsertedCard(String player, Card card, List<Card> deck) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            final int index = deck.indexOf(card);
            return String.format(
                "You inserted the card %s into the deck at position %d.", formatCard(card), index);
          } else {
            return String.format(
                "Player %s inserted the card %s into the deck.",
                formatPlayer(player), formatCard(card));
          }
        });
  }

  @Override
  public void playerExploded(String player) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return ":boom: You exploded!";
          } else {
            return String.format(":boom: Player %s exploded.", formatPlayer(player));
          }
        });
  }

  @Override
  public void playerWonTheGame(String player) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return "You won the game! :tada:";
          } else {
            return String.format("Player %s won the game! :tada:", formatPlayer(player));
          }
        });
  }

  @Override
  public void playerNoped(String player, List<Card> nopedCards) {
    sendMessage(
        user -> {
          if (player.equals(user)) {
            return String.format("You noped the card(s) %s.", formatCards(nopedCards));
          } else {
            return String.format(
                "Player %s noped the card(s) %s.", formatPlayer(player), formatCards(nopedCards));
          }
        });
  }
}
