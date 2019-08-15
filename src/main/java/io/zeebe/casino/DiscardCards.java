package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;

public class DiscardCards implements JobHandler {

  private final Logger log;

  public DiscardCards(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final var cards = (List<String>) variables.get("cards");
    final var discardPile = (List<String>) variables.computeIfAbsent("discardPile", (key) ->
        new ArrayList<String>());

    log.info("Discard {}", cards);
    discardPile.addAll(cards);

    variables.put("cards", Collections.EMPTY_LIST);
    variables.put("discardPile", discardPile);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(variables).send();
  }
}
