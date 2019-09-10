package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.concurrent.ThreadLocalRandom;

public class SelectCardFromPlayer implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  public SelectCardFromPlayer(GameListener listener,
      GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var otherPlayer = variables.getOtherPlayer();
    final var players = variables.getPlayers();
    final var otherHand = players.get(otherPlayer);

    if (!otherHand.isEmpty()) {

      if (otherHand.size() == 1) {

        final var card = otherHand.get(0);
        completeJob(jobClient, job, variables, currentPlayer, otherPlayer, card);

      } else {
        interaction.selectCardToGive(otherPlayer, otherHand)
            .thenAccept(card -> completeJob(jobClient, job, variables, currentPlayer, otherPlayer, card));
      }

    } else {
      variables.putCard(null);

      jobClient
          .newCompleteCommand(job.getKey())
          .variables(variables.getResultVariables())
          .send()
          .join();
    }
  }

  private void completeJob(JobClient jobClient, ActivatedJob job, Variables variables,
      String currentPlayer, String otherPlayer, Card card) {
    listener.cardChosenFrom(GameContext.of(job), currentPlayer, otherPlayer, card);

    variables.putCard(card);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
