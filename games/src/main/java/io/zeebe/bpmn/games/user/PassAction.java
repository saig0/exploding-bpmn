package io.zeebe.bpmn.games.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;

public class PassAction implements JobHandler {


  private final Logger log;

  public PassAction(Logger log) {
    this.log = log;
  }


  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final String nextPlayer = variables.get("nextPlayer").toString();
    log.info("Player {} passes.", nextPlayer);

    jobClient.newCompleteCommand(activatedJob.getKey()).send();
  }
}
