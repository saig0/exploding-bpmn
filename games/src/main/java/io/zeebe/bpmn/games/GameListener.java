package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import java.util.List;
import java.util.Map;

public interface GameListener {

  void newGameStarted(List<String> playerNames);

  void handCardsDealt(Map<String, List<Card>> handCards);

  void nextPlayerSelected(String player, int turns);

  void cardsPlayed(String player, List<Card> cards);

  void playerPassed(String player);

  void playerDrawnCard(String player, Card card);

  void turnEnded(String player, int remainingTurns);

  void cardsDiscarded(String player, List<Card> cards);

  void playerToDrawSelected(String player, String playerToDrawFrom);

  void cardTakenFrom(String player, String playerTakenFrom, Card card);

  void cardChosenFrom(String player, String playerChosenFrom, Card card);

  void playerSawTheFuture(String player, List<Card> cards);

  void deckShuffled(List<Card> deck);

  void playerAlteredTheFuture(String player, List<Card> cards);

  void deckReordered(List<Card> deck);

  void handCheckedForDefuse(String player, List<Card> hand);

  void playerInsertedCard(String player, Card card, List<Card> deck);

  void playerExploded(String player);

  void playerWonTheGame(String player);

  void playerNoped(String player, List<Card> nopedCards);
}
