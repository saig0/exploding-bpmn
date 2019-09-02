package io.zeebe.bpmn.games.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class ShuffleDeck implements JobHandler {

  private final Logger log;

  public ShuffleDeck(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();
    final var deck = (List<String>) variables.get("deck");

    log.info("Shuffle deck");

    Collections.shuffle(deck);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("deck", deck)).send();
  }
}
