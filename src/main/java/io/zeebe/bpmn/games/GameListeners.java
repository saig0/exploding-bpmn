package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Primary
public class GameListeners implements GameListener {

  private final List<GameListener> listeners;

  public GameListeners(List<GameListener> listeners) {
    this.listeners = listeners;
  }

  @Override
  public void newGameStarted(Context context, List<String> playerNames) {
    listeners.forEach(l -> l.newGameStarted(context, playerNames));
  }

  @Override
  public void handCardsDealt(Context context, Map<String, List<Card>> handCards) {
    listeners.forEach(l -> l.handCardsDealt(context, handCards));
  }

  @Override
  public void nextPlayerSelected(Context context, String player, int turns) {
    listeners.forEach(l -> l.nextPlayerSelected(context, player, turns));
  }

  @Override
  public void cardsPlayed(Context context, String player, List<Card> cards) {
    listeners.forEach(l -> l.cardsPlayed(context, player, cards));
  }

  @Override
  public void playerPassed(Context context, String player) {
    listeners.forEach(l -> l.playerPassed(context, player));
  }

  @Override
  public void playerDrawnCardFromBottom(Context context, String player, Card card) {
    listeners.forEach(l -> l.playerDrawnCardFromBottom(context, player, card));
  }

  @Override
  public void playerDrawnCardFromTop(GameContext context, String player, Card card) {
    listeners.forEach(l -> l.playerDrawnCardFromTop(context, player, card));
  }

  @Override
  public void turnEnded(Context context, String player, int remainingTurns) {
    listeners.forEach(l -> l.turnEnded(context, player, remainingTurns));
  }

  @Override
  public void cardsDiscarded(Context context, String player, List<Card> cards) {
    listeners.forEach(l -> l.cardsDiscarded(context, player, cards));
  }

  @Override
  public void playerToDrawSelected(Context context, String player, String playerToDrawFrom) {
    listeners.forEach(l -> l.playerToDrawSelected(context, player, playerToDrawFrom));
  }

  @Override
  public void cardTakenFrom(Context context, String player, String playerTakenFrom, Card card) {
    listeners.forEach(l -> l.cardTakenFrom(context, player, playerTakenFrom, card));
  }

  @Override
  public void cardChosenFrom(Context context, String player, String playerChosenFrom, Card card) {
    listeners.forEach(l -> l.cardChosenFrom(context, player, playerChosenFrom, card));
  }

  @Override
  public void playerSawTheFuture(Context context, String player, List<Card> cards) {
    listeners.forEach(l -> l.playerSawTheFuture(context, player, cards));
  }

  @Override
  public void deckShuffled(Context context, List<Card> deck) {
    listeners.forEach(l -> l.deckShuffled(context, deck));
  }

  @Override
  public void playerAlteredTheFuture(Context context, String player, List<Card> cards) {
    listeners.forEach(l -> l.playerAlteredTheFuture(context, player, cards));
  }

  @Override
  public void deckReordered(Context context, List<Card> deck) {
    listeners.forEach(l -> l.deckReordered(context, deck));
  }

  @Override
  public void handCheckedForDefuse(Context context, String player, List<Card> hand) {
    listeners.forEach(l -> l.handCheckedForDefuse(context, player, hand));
  }

  @Override
  public void playerInsertedCard(Context context, String player, Card card, List<Card> deck) {
    listeners.forEach(l -> l.playerInsertedCard(context, player, card, deck));
  }

  @Override
  public void playerExploded(Context context, String player) {
    listeners.forEach(l -> l.playerExploded(context, player));
  }

  @Override
  public void playerWonTheGame(Context context, String player) {
    listeners.forEach(l -> l.playerWonTheGame(context, player));
  }

  @Override
  public void playerNoped(Context context, String player, List<Card> nopedCards) {
    listeners.forEach(l -> l.playerNoped(context, player, nopedCards));
  }

  @Override
  public void gameCanceled(Context context) {
    listeners.forEach(l -> l.gameCanceled(context));
  }
}
