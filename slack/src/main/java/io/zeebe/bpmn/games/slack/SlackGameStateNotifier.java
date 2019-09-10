package io.zeebe.bpmn.games.slack;

import static io.zeebe.bpmn.games.slack.SlackUtil.formatCard;
import static io.zeebe.bpmn.games.slack.SlackUtil.formatCards;
import static io.zeebe.bpmn.games.slack.SlackUtil.formatPlayer;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlackGameStateNotifier implements GameListener {

  private static final Logger LOG = LoggerFactory.getLogger(SlackGameStateNotifier.class);

  @Autowired private SlackSession session;

  @Autowired private MethodsClient methodsClient;

  private void gameEnded(Context context) {
    session.removeGame(context.getKey());
  }

  private void sendMessageTo(String channelId, String message) {
    try {

      final var resp = methodsClient.chatPostMessage(req -> req.channel(channelId).text(message));

      if (!resp.isOk()) {
        LOG.warn("Failed to send message: {}", resp);
      }

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void sendMessage(Context context, Function<String, String> messageForUser) {

    final List<String> userIds = session.getUserIdsOfGame(context.getKey());

    userIds.forEach(
        userId -> {
          final var channelId = session.getChannelId(userId);

          final var message = messageForUser.apply(userId);

          sendMessageTo(channelId, message);
        });
  }

  @Override
  public void newGameStarted(Context context, List<String> playerNames) {
    sendMessage(
        context,
        user -> {
          final var otherPlayers =
              playerNames.stream()
                  .filter(player -> !player.equals(user))
                  .map(SlackUtil::formatPlayer)
                  .collect(Collectors.joining(", "));

          return String.format("New game :boom: :cat2: with %s", otherPlayers);
        });
  }

  @Override
  public void handCardsDealt(Context context, Map<String, List<Card>> handCards) {
    sendMessage(
        context,
        user -> {
          final var hand = handCards.get(user);
          return String.format("Your hand: %s", formatCards(hand));
        });
  }

  @Override
  public void nextPlayerSelected(Context context, String player, int turns) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format("You are next for %d turn(s)", turns);
          } else {
            return String.format("Next is %s for %d turn(s)", formatPlayer(player), turns);
          }
        });
  }

  @Override
  public void cardsPlayed(Context context, String player, List<Card> cards) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format("You played %s", formatCards(cards));
          } else {
            return String.format("%s played %s.", formatPlayer(player), formatCards(cards));
          }
        });
  }

  @Override
  public void playerPassed(Context context, String player) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return "You passed.";
          } else {
            return String.format("%s passed.", formatPlayer(player));
          }
        });
  }

  @Override
  public void playerDrawnCard(Context context, String player, Card card) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format("You draw %s", formatCard(card));
          } else if (card.getType() == CardType.EXPLODING) {
            return String.format("%s draw %s", formatPlayer(player), formatCard(card));
          } else {
            return String.format("%s draw a card.", formatPlayer(player));
          }
        });
  }

  @Override
  public void turnEnded(Context context, String player, int remainingTurns) {}

  @Override
  public void cardsDiscarded(Context context, String player, List<Card> cards) {}

  @Override
  public void playerToDrawSelected(Context context, String player, String playerToDrawFrom) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format(
                "You chose %s to draw a card from.", formatPlayer(playerToDrawFrom));
          } else if (playerToDrawFrom.equals(user)) {
            return String.format("%s chose you to draw a card from.", formatPlayer(player));
          } else {
            return String.format(
                "%s chose %s to draw a card form.",
                formatPlayer(player), formatPlayer(playerToDrawFrom));
          }
        });
  }

  @Override
  public void cardTakenFrom(Context context, String player, String playerTakenFrom, Card card) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format(
                "You took %s from %s.", formatCard(card), formatPlayer(playerTakenFrom));
          } else if (playerTakenFrom.equals(user)) {
            return String.format("%s took %s from you.", formatPlayer(player), formatCard(card));
          } else {
            return String.format(
                "%s took a card form %s.", formatPlayer(player), formatPlayer(playerTakenFrom));
          }
        });
  }

  @Override
  public void cardChosenFrom(Context context, String player, String playerChosenFrom, Card card) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format(
                "You got %s from %s.", formatCard(card), formatPlayer(playerChosenFrom));
          } else if (playerChosenFrom.equals(user)) {
            return String.format("%s got %s from you.", formatPlayer(player), formatCard(card));
          } else {
            return String.format(
                "%s got a card form %s.", formatPlayer(player), formatPlayer(playerChosenFrom));
          }
        });
  }

  @Override
  public void playerSawTheFuture(Context context, String player, List<Card> cards) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format(
                "The future (the top 3 cards of the deck): %s", formatCards(cards));
          } else {
            return String.format(
                "%s saw the future (the top 3 cards of the deck)", formatPlayer(player));
          }
        });
  }

  @Override
  public void deckShuffled(Context context, List<Card> deck) {
    sendMessage(context, user -> "The deck is shuffled.");
  }

  @Override
  public void playerAlteredTheFuture(Context context, String player, List<Card> cards) {
    final var channelId = session.getChannelId(player);
    sendMessageTo(channelId, String.format("You altered the future to %s", formatCards(cards)));
  }

  @Override
  public void deckReordered(Context context, List<Card> deck) {
    sendMessage(
        context, user -> "The future was altered (the order of the top 3 cards has changed)");
  }

  @Override
  public void handCheckedForDefuse(Context context, String player, List<Card> hand) {}

  @Override
  public void playerInsertedCard(Context context, String player, Card card, List<Card> deck) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            final int index = deck.indexOf(card) + 1;
            return String.format(
                "You inserted %s into the deck at position %d.", formatCard(card), index);
          } else {
            return String.format(
                "%s inserted %s into the deck.", formatPlayer(player), formatCard(card));
          }
        });
  }

  @Override
  public void playerExploded(Context context, String player) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return "You exploded :boom:";
          } else {
            return String.format("%s exploded :boom:", formatPlayer(player));
          }
        });
  }

  @Override
  public void playerWonTheGame(Context context, String player) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return "You won the game :tada:";
          } else {
            return String.format("%s won the game :tada:", formatPlayer(player));
          }
        });

    final String losers =
        session.getUserIdsOfGame(context.getKey()).stream()
            .filter(user -> !user.equals(player))
            .map(SlackUtil::formatPlayer)
            .collect(Collectors.joining(", "));

    sendMessageTo(
        session.getGameChannelId(context.getKey()),
        String.format(":tada: %s won against %s :boom:", formatPlayer(player), losers));

    gameEnded(context);
  }

  @Override
  public void playerNoped(Context context, String player, List<Card> nopedCards) {
    sendMessage(
        context,
        user -> {
          if (player.equals(user)) {
            return String.format("You noped %s.", formatCards(nopedCards));
          } else {
            return String.format("%s noped %s.", formatPlayer(player), formatCards(nopedCards));
          }
        });
  }
}
