package io.zeebe.bpmn.games.deck;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Optional;

public class InitGame implements JobHandler {

  private final GameListener listener;

  public InitGame(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var playerNames =
        Optional.ofNullable(variables.getPlayerNames())
            .orElseThrow(() -> new RuntimeException("Missing variable with name 'playerNames'."));

    final var playerCount = playerNames.size();
    if (playerCount < 2 || playerCount > 10) {
      throw new RuntimeException(
          String.format("Expected between 2 and 10 players but was {}", playerCount));
    }

    listener.newGameStarted(GameContext.of(job), playerNames);

    variables
        .putRound(0)
        .putTurns(1)
        .putCorrelationKey(String.valueOf(job.getWorkflowInstanceKey()));

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
