package io.zeebe.casino.deck;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class DiscardNope implements JobHandler {

  private final Logger log;

  public DiscardNope(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();
    final var discardPile =
        (List<String>) variables.computeIfAbsent("discardPile", (key) -> new ArrayList<String>());

    final String playerWhoNoped = variables.get("playerNoped").toString();
    final var players = (Map<String, List<String>>) variables.get("players");
    final List<String> hand = players.get(playerWhoNoped);

    hand.remove("nope");
    players.put(playerWhoNoped, hand);
    discardPile.add("nope");

    log.info("Remove nope from players hand: {}", playerWhoNoped);

    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(
            Map.of(
                "discardPile", discardPile,
                "players", players))
        .send();
  }
}
