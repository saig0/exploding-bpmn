package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AtomicAction implements JobHandler {

  private final GameListener listener;

  @Autowired
  public AtomicAction(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "atomic")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var deck = variables.getDeck();
    // Move all exploding cards to the top of the deck
    final var explodingKittens =
        deck.stream().filter(card -> card.getType() == CardType.EXPLODING).toList();
    deck.removeAll(explodingKittens);
    deck.addAll(0, explodingKittens);

    variables.putDeck(deck);

    listener.explodingKittensMovedToTopOfDeck(GameContext.of(job), deck);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
