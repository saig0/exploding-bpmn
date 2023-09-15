package io.zeebe.bpmn.games.bot;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class BotMemoryListener implements GameListener, BotMemoryAccess {

  private final Map<String, BotMemory> memoryByGame = new HashMap<>();

  private BotMemory getMemory(Context context) {
    return memoryByGame.computeIfAbsent(context.getKey(), key -> new BotMemory());
  }

  @Override
  public Optional<Card> getNextCard(String gameKey, String player) {
    return Optional.ofNullable(memoryByGame.get(gameKey))
        .flatMap(memory -> memory.getNextCard(player));
  }

  @Override
  public void newGameStarted(Context context, List<String> playerNames) {
    // no-op
  }

  @Override
  public void handCardsDealt(Context context, Map<String, List<Card>> handCards) {
    // no-op
  }

  @Override
  public void nextPlayerSelected(Context context, String player, int turns) {
    // no-op
  }

  @Override
  public void cardsPlayed(Context context, String player, List<Card> cards) {
    // no-op
  }

  @Override
  public void playerPassed(Context context, String player) {
    // no-op
  }

  @Override
  public void playerDrawnCardFromBottom(Context context, String player, Card card) {
    // no-op
  }

  @Override
  public void playerDrawnCardFromTop(GameContext context, String player, Card card) {
    getMemory(context).removeNextCard();
  }

  @Override
  public void turnEnded(Context context, String player, int remainingTurns) {
    // no-op
  }

  @Override
  public void cardsDiscarded(Context context, String player, List<Card> cards) {
    // no-op
  }

  @Override
  public void playerToDrawSelected(Context context, String player, String playerToDrawFrom) {
    // no-op
  }

  @Override
  public void cardTakenFrom(Context context, String player, String playerTakenFrom, Card card) {
    // no-op
  }

  @Override
  public void cardChosenFrom(Context context, String player, String playerChosenFrom, Card card) {
    // no-op
  }

  @Override
  public void playerSawTheFuture(Context context, String player, List<Card> cards) {
    getMemory(context).rememberFuture(player, cards);
  }

  @Override
  public void deckShuffled(Context context, List<Card> deck) {
    getMemory(context).forgetAll();
  }

  @Override
  public void playerAlteredTheFuture(Context context, String player, List<Card> cards) {
    getMemory(context).forgetNextCards(cards.size());
    getMemory(context).rememberFuture(player, cards);
  }

  @Override
  public void deckReordered(Context context, List<Card> deck) {
    // no-op
  }

  @Override
  public void handCheckedForDefuse(Context context, String player, List<Card> hand) {
    // no-op
  }

  @Override
  public void playerInsertedCard(Context context, String player, Card card, List<Card> deck) {
    getMemory(context).forgetAll();

    final int cardIndex = deck.indexOf(card);
    getMemory(context).rememberCard(player, card, cardIndex);
  }

  @Override
  public void playerExploded(Context context, String player) {
    // no-op
  }

  @Override
  public void playerWonTheGame(Context context, String player) {
    memoryByGame.remove(context.getKey());
  }

  @Override
  public void playerNoped(Context context, String player, List<Card> nopedCards) {
    // no-op
  }

  @Override
  public void gameCanceled(Context context) {
    memoryByGame.remove(context.getKey());
  }

  @Override
  public void explodingKittensMovedToTopOfDeck(Context context, List<Card> explodingKittens, List<Card> deck) {
    getMemory(context).forgetAll();

    getMemory(context).rememberFutureAll(explodingKittens);
  }
}
