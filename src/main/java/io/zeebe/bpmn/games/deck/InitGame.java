package io.zeebe.bpmn.games.deck;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

@Component
public class InitGame implements JobHandler {

  private final GameListener listener;

  public InitGame(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "initGame")
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

    // start with a random player
    final var nextPlayerIndex = ThreadLocalRandom.current().nextInt(playerCount);

    listener.newGameStarted(GameContext.of(job), playerNames);

    variables
        .putPlayerNames(playerNames)
        .putRound(0)
        .putTurns(1)
        .putNextPlayerIndex(nextPlayerIndex)
        .putCorrelationKey(String.valueOf(job.getProcessInstanceKey()));

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
