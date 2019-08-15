package io.zeebe.casino.action;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class UpdateDeck implements JobHandler {

  private final Logger log;

  public UpdateDeck(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();
    final var deck = ((List<String>) variables.get("deck"));
    final var alternativeOrder = ((List<String>) variables.get("alternativeOrder"));

    final var cards = Math.min(3, alternativeOrder.size());

    final var subDeck = deck.subList(cards, deck.size());
    subDeck.addAll(0, alternativeOrder);

    log.info(
        "Player {} updates deck with new order of first three cards.", variables.get("nextPlayer"));

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of(
        "deck", subDeck)).send();
  }
}
