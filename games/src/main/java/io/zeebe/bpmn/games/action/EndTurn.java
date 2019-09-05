package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class EndTurn implements JobHandler {

  private final GameListener listener;

  public EndTurn(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final int turns = variables.getTurns();
    final var remainingTurns = turns - 1;

    variables.putTurns(remainingTurns);

    listener.turnEnded(GameContext.of(job), variables.getNextPlayer(), remainingTurns);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
