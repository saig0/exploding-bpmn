package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Map;
import org.slf4j.Logger;

public class AddTurns implements JobHandler {

  public AddTurns(Logger log) {}

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final int turns = (int) variables.get("turns");

    // -1 turn, because end of the turn
    // +2 turn because of attack
    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(Map.of("turns", turns + 1))
        .send();
  }
}
