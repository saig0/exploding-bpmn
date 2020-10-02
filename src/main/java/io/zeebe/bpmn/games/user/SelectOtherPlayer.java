package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectOtherPlayer implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public SelectOtherPlayer(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "selectOtherPlayer")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();

    final var playerNames = variables.getPlayerNames();
    playerNames.remove(currentPlayer);

    if (playerNames.size() == 1) {
      final var otherPlayer = playerNames.get(0);
      completeJob(jobClient, job, variables, currentPlayer, otherPlayer);
    } else {
      interaction
          .selectPlayer(currentPlayer, playerNames)
          .thenAccept(
              otherPlayer -> completeJob(jobClient, job, variables, currentPlayer, otherPlayer));
    }
  }

  private void completeJob(
      JobClient jobClient,
      ActivatedJob job,
      Variables variables,
      String currentPlayer,
      String otherPlayer) {

    listener.playerToDrawSelected(GameContext.of(job), currentPlayer, otherPlayer);

    variables.putOtherPlayer(otherPlayer);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
