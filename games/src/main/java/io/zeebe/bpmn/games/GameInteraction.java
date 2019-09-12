package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GameInteraction {

  CompletableFuture<List<Card>> selectCardsToPlay(String player, List<Card> handCards, int deckSize,
      String nextPlayer);

  CompletableFuture<Boolean> nopeThePlayedCard(String player);

  CompletableFuture<List<Card>> alterTheFuture(String player, List<Card> cards);

  CompletableFuture<String> selectPlayer(String player, List<String> otherPlayers);

  CompletableFuture<Card> selectCardToGive(String player, List<Card> handCards);

  CompletableFuture<Integer> selectPositionToInsertCard(String player,
      Card card, int deckSize);

}
