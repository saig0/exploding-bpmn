package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class UpdateDeck implements JobHandler {

  private final GameListener listener;

  public UpdateDeck(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var deck = variables.getDeck();
    final var cards = variables.getCards();

    final var updatedDeck = deck.subList(cards.size(), deck.size());
    updatedDeck.addAll(0, cards);

    listener.deckReordered(GameContext.of(job), updatedDeck);

    variables.putDeck(updatedDeck);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
