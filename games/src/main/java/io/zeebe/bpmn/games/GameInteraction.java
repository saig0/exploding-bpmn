package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface GameInteraction {

  CompletableFuture<List<Card>> selectCardsToPlay(String player, List<Card> handCards);

}
