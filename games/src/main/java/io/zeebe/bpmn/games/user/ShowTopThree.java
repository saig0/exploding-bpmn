package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class ShowTopThree implements JobHandler {

  private final GameListener listener;

  public ShowTopThree(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var deck = variables.getDeck();

    final var amount = Math.min(3, deck.size());
    final var cards = deck.subList(0, amount);

    listener.playerSawTheFuture(GameContext.of(job), currentPlayer, cards);

    jobClient
        .newCompleteCommand(job.getKey())
        .send()
        .join();
  }
}
