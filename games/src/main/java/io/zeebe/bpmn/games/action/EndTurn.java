package io.zeebe.bpmn.games.action;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Map;
import org.slf4j.Logger;

public class EndTurn implements JobHandler {

  private final Logger log;

  public EndTurn(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final int turns = (int) variables.get("turns");

    final var remainingTurns = turns - 1;
    log.info("End turn, remaining turns {}", remainingTurns);

    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(Map.of("turns", remainingTurns))
        .send();
  }
}
