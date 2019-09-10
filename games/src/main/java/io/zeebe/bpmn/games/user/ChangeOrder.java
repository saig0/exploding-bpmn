package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;

public class ChangeOrder implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  public ChangeOrder(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var deck = variables.getDeck();

    final var amount = Math.min(3, deck.size());
    final var cards = deck.subList(0, amount);

    interaction
        .alterTheFuture(currentPlayer, cards)
        .thenAccept(
            alteredFuture -> completeJob(jobClient, job, variables, currentPlayer, alteredFuture));
  }

  private void completeJob(
      JobClient jobClient,
      ActivatedJob job,
      Variables variables,
      String currentPlayer,
      List<Card> cards) {

    listener.playerAlteredTheFuture(GameContext.of(job), currentPlayer, cards);

    variables.putCards(cards);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
