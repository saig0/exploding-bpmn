package io.zeebe.bpmn.games.deck;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiscardCards implements JobHandler {

  private final GameListener listener;

  @Autowired
  public DiscardCards(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "discard")
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

    variables.putPlayers(players).putDiscardPile(discardPile).putCards(List.of());

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
