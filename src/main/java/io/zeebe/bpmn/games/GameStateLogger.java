package io.zeebe.bpmn.games;

import com.google.common.base.Strings;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.slack.SlackGameStateNotifier;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(SlackGameStateNotifier.class)
public class GameStateLogger implements GameListener {

  private final Logger LOG = LoggerFactory.getLogger(GameStateLogger.class);

  @Override
  public void newGameStarted(Context context, List<String> playerNames) {
    LOG.debug("New game with players: {}", playerNames);
  }

  @Override
  public void handCardsDealt(Context context, Map<String, List<Card>> handCards) {
    LOG.debug("Hand cards dealt: {}", handCards);
  }

  @Override
  public void nextPlayerSelected(Context context, String player, int turns) {
    LOG.debug("Next player: {} for {} turn(s)", player, turns);
  }

  @Override
  public void cardsPlayed(Context context, String player, List<Card> cardsToPlay) {
    LOG.debug("Player {} selected {} to play.", player, cardsToPlay);
  }

  @Override
  public void playerPassed(Context context, String player) {
    LOG.debug("Player {} passed its turn.", player);
  }

  @Override
  public void playerDrawnCardFromBottom(Context context, String player, Card card) {
    LOG.debug("Player {} drawn card {} from the bottom.", player, card);
  }

  @Override
  public void playerDrawnCardFromTop(GameContext context, String player, Card card) {
    LOG.debug("Player {} drawn card {} from the top.", player, card);
  }

  @Override
  public void turnEnded(Context context, String player, int remainingTurns) {
    LOG.debug("Player {}'s turn ended. Remaining turns {}.", player, remainingTurns);
  }

  @Override
  public void cardsDiscarded(Context context, String player, List<Card> cards) {
    LOG.debug("Player {} put the cards {} onto the discard pile.", player, cards);
  }

  @Override
  public void playerToDrawSelected(Context context, String player, String playerToDrawFrom) {
    LOG.debug("Player {} selected player {} to draw a card from.", player, playerToDrawFrom);
  }

  @Override
  public void cardTakenFrom(Context context, String player, String playerTakenFrom, Card card) {
    LOG.debug("Player {} took the card {} from player {}.", player, card, playerTakenFrom);
  }

  @Override
  public void cardChosenFrom(Context context, String player, String playerChosenFrom, Card card) {
    LOG.debug("Player {} choose the card {} from player {}.", player, card, playerChosenFrom);
  }

  @Override
  public void playerSawTheFuture(Context context, String player, List<Card> cards) {
    LOG.debug("Player {} saw the future {}.", player, cards);
  }

  @Override
  public void deckShuffled(Context context, List<Card> deck) {
    LOG.debug("Deck was shuffled {}.", deck);
  }

  @Override
  public void playerAlteredTheFuture(Context context, String player, List<Card> cards) {
    LOG.debug("Player {} altered the future {}.", player, cards);
  }

  @Override
  public void deckReordered(Context context, List<Card> deck) {
    LOG.debug("Deck was reordered {}.", deck);
  }

  @Override
  public void handCheckedForDefuse(Context context, String player, List<Card> hand) {
    LOG.debug("Checked player {}'s hand {} for a defuse card.", player, hand);
  }

  @Override
  public void playerInsertedCard(Context context, String player, Card card, List<Card> deck) {
    LOG.debug("Player {} inserted the card {} into the deck {}.", player, card, deck);
  }

  @Override
  public void playerExploded(Context context, String player) {
    LOG.debug("Player {} exploded.", player);
  }

  @Override
  public void playerWonTheGame(Context context, String player) {
    final int count = 19 - player.length();
    LOG.debug(
        "\n"
            + "=========================================="
            + "\n============= WINNER: {} "
            + Strings.repeat("=", count > 0 ? count : 0)
            + "\n==========================================",
        player);
  }

  @Override
  public void playerNoped(Context context, String player, List<Card> nopedCards) {
    LOG.debug("Player {} noped the card(s) {}.", player, nopedCards);
  }

  @Override
  public void gameCanceled(final Context context) {
    LOG.debug("The game is canceled.");
  }

  @Override
  public void explodingKittensMovedToTopOfDeck(
      Context context, List<Card> explodingKittens, List<Card> deck) {
    LOG.debug("All exploding kittens are placed at the top of the deck {}", deck);
  }
}
