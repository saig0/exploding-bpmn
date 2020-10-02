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
public class SelectPlayer implements JobHandler {

  private final GameListener listener;

  @Autowired
  public SelectPlayer(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "selectPlayerForNewRound")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final int round = variables.getRound();

    final var playerNames = variables.getPlayerNames();
    final var currentPlayerIndex = variables.getNextPlayerIndex();
    final var currentPlayer = playerNames.get(currentPlayerIndex);

    final int turns = variables.getTurns();

    listener.nextPlayerSelected(GameContext.of(job), currentPlayer, turns);

    variables
        .putRound(round + 1)
        .putNextPlayer(currentPlayer)
        .putNextPlayerIndex((currentPlayerIndex + 1) % playerNames.size())
        .putTurnArray(turns) // used for multi-instance
        .putNopedPlayer(null);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
