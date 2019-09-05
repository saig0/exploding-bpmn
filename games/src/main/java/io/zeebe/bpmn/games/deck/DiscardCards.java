package io.zeebe.bpmn.games.deck;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;

public class DiscardCards implements JobHandler {

  private final GameListener listener;

  public DiscardCards(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var cards = variables.getCards();
    final var discardPile = variables.getDiscardPile();

    final String currentPlayer = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(currentPlayer);

    hand.removeAll(cards);
    discardPile.addAll(cards);

    listener.cardsDiscarded(GameContext.of(job), currentPlayer, cards);

    variables
        .putPlayers(players)
        .putDiscardPile(discardPile)
        .putCards(List.of());

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
