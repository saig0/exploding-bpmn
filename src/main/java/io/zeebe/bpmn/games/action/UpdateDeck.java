package io.zeebe.bpmn.games.action;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateDeck implements JobHandler {

  private final GameListener listener;

  @Autowired
  public UpdateDeck(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "updateDeck")
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
