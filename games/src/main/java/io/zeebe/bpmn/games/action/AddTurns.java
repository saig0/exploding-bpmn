package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class AddTurns implements JobHandler {

  private final GameListener listener;

  public AddTurns(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    // when ATTACK was played then it ends the current player turns
    listener.turnEnded(variables.getNextPlayer(), 0);

    // the next player has 2 extra turns
    final int turns = variables.getTurns();
    // -1 turn, because end of the turn
    // +2 turn because of attack
    variables.putTurns(turns + 1);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
