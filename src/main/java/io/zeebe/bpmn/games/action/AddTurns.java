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
public class AddTurns implements JobHandler {

  private final GameListener listener;

  @Autowired
  public AddTurns(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "addTurns")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    // when ATTACK was played then it ends the current player turns
    listener.turnEnded(GameContext.of(job), variables.getNextPlayer(), 0);

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
