package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;

public class ChangeOrder implements JobHandler {

  private final GameListener listener;

  public ChangeOrder(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var deck = variables.getDeck();

    final var amount = Math.min(3, deck.size());
    final var cards = deck.subList(0, amount);

    Collections.shuffle(cards);

    listener.playerAlteredTheFuture(GameContext.of(job), currentPlayer, cards);

    variables.putCards(cards);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
