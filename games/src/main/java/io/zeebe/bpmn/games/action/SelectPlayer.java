package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;

public class SelectPlayer implements JobHandler {

  private final GameListener listener;

  public SelectPlayer(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final int round = variables.getRound();

    final var playerNames = variables.getPlayerNames();
    final var nextPlayer = playerNames.get(round % playerNames.size());

    final int turns = variables.getTurns();

    listener.nextPlayerSelected(GameContext.of(job), nextPlayer, turns);

    variables
        .putRound(round + 1)
        .putNextPlayer(nextPlayer)
        .putTurnArray(turns)  // used for multi-instance
        .putNopedPlayer(null);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
