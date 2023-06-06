package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.NopeTurn;
import io.zeebe.bpmn.games.model.PlayerTurn;
import io.zeebe.bpmn.games.model.PlayersOverview;
import io.zeebe.bpmn.games.slack.SlackUserInteraction;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnMissingBean(SlackUserInteraction.class)
public class NoopUserInteraction implements UserInteraction {
  @Override
  public CompletableFuture<List<Card>> selectCardsToPlay(PlayerTurn playerTurn) {
    return CompletableFuture.completedFuture(Collections.emptyList());
  }

  @Override
  public CompletableFuture<Boolean> nopeThePlayedCard(NopeTurn nopeTurn) {
    return CompletableFuture.completedFuture(false);
  }

  @Override
  public CompletableFuture<List<Card>> alterTheFuture(PlayerTurn playerTurn, List<Card> cards) {
    return CompletableFuture.completedFuture(cards);
  }

  @Override
  public CompletableFuture<String> selectPlayer(String player, PlayersOverview playersOverview) {
    final var nextPlayer = playersOverview.getPlayers().get(0);
    return CompletableFuture.completedFuture(nextPlayer.getName());
  }

  @Override
  public CompletableFuture<Card> selectCardToGive(String player, List<Card> handCards) {
    return CompletableFuture.completedFuture(handCards.get(0));
  }

  @Override
  public CompletableFuture<Integer> selectPositionToInsertExplodingCard(PlayerTurn playerTurn, Card card) {
    return CompletableFuture.completedFuture(0);
  }
}
