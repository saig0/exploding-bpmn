package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.NopeTurn;
import io.zeebe.bpmn.games.model.PlayerTurn;
import io.zeebe.bpmn.games.model.PlayersOverview;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GameInteraction {

  CompletableFuture<List<Card>> selectCardsToPlay(PlayerTurn playerTurn);

  CompletableFuture<Boolean> nopeThePlayedCard(NopeTurn nopeTurn);

  CompletableFuture<List<Card>> alterTheFuture(PlayerTurn playerTurn, List<Card> cards);

  CompletableFuture<String> selectPlayer(String player, PlayersOverview playersOverview);

  CompletableFuture<Card> selectCardToGive(String player, List<Card> handCards);

  CompletableFuture<Integer> selectPositionToInsertExplodingCard(PlayerTurn playerTurn, Card card);
}
