package io.zeebe.casino;

import com.google.common.collect.Maps;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of(
        "cards", List.of(),
        "discardPile", discardPile
    )).send();
  }
}
