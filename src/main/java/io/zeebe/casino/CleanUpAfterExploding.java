package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class CleanUpAfterExploding implements JobHandler {

  private final Logger log;

  public CleanUpAfterExploding(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();

    final String currentPlayer = variables.get("nextPlayer").toString();
    final Map players = (Map) variables.get("players");
    final var hand = (List<String>) players.remove(currentPlayer);

    final var discardPile = (List<String>) variables.computeIfAbsent("discardPile", (key) ->
        new ArrayList<String>());

    discardPile.addAll(hand);

    log.info("Remove player {} from game.", currentPlayer);

    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(Map.of("discardPile", discardPile, "players", players, "card", null))
        .send();
  }
}
