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
}
