package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Map;
import org.slf4j.Logger;

public class NewTurn implements JobHandler {

  private final Logger log;

  public NewTurn(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    var turns = (int) variables.get("turns");

    turns += 1;

    log.info("New turns {}", turns);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("turns", turns)).send();
  }
}
