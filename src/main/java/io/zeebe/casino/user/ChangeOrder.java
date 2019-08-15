package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;

public class ChangeOrder implements JobHandler {

  private final Logger log;

  public ChangeOrder(Logger log) {
    this.log = log;
  }


  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();
    final var deck = (List<String>) variables.get("deck");

    final List<String> alternativeOrder = deck.subList(0, 3);

    Collections.shuffle(alternativeOrder, new Random());

    log.info("Player {} changed order of first three cards.", variables.get("nextPlayer"));

    jobClient.newCompleteCommand(activatedJob.getKey())
        .variables(Map.of(
            "alternativeOrder", alternativeOrder))
        .send();
  }
}
