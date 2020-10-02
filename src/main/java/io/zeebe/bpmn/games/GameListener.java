package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import java.util.List;
import java.util.Map;

public interface GameListener {

  void newGameStarted(Context context, List<String> playerNames);

  void handCardsDealt(Context context, Map<String, List<Card>> handCards);

  void nextPlayerSelected(Context context, String player, int turns);

  void cardsPlayed(Context context, String player, List<Card> cards);

  void playerPassed(Context context, String player);

  void playerDrawnCard(Context context, String player, Card card);

  void turnEnded(Context context, String player, int remainingTurns);

  void cardsDiscarded(Context context, String player, List<Card> cards);

  void playerToDrawSelected(Context context, String player, String playerToDrawFrom);

  void cardTakenFrom(Context context, String player, String playerTakenFrom, Card card);

  void cardChosenFrom(Context context, String player, String playerChosenFrom, Card card);

  void playerSawTheFuture(Context context, String player, List<Card> cards);

  void deckShuffled(Context context, List<Card> deck);

  void playerAlteredTheFuture(Context context, String player, List<Card> cards);

  void deckReordered(Context context, List<Card> deck);

  void handCheckedForDefuse(Context context, String player, List<Card> hand);

  void playerInsertedCard(Context context, String player, Card card, List<Card> deck);

  void playerExploded(Context context, String player);

  void playerWonTheGame(Context context, String player);

  void playerNoped(Context context, String player, List<Card> nopedCards);

  void gameCanceled(Context context);

  interface Context {
    String getKey();
  }
}
