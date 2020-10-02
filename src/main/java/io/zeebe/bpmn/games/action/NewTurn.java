package io.zeebe.bpmn.games.action;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.model.Variables;
import org.springframework.stereotype.Component;

@Component
public class NewTurn implements JobHandler {

  @ZeebeWorker(type = "newTurn")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    var turns = variables.getTurns();

    variables.putTurns(turns + 1);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
