package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;

public class ShuffleDeck implements JobHandler {

  private final GameListener listener;

  public ShuffleDeck(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var deck = variables.getDeck();

    Collections.shuffle(deck);

    listener.deckShuffled(deck);

    variables.putDeck(deck);

    jobClient.newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
