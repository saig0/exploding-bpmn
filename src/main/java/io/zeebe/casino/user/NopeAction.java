package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class NopeAction implements JobHandler {

  private final Logger log;

  public NopeAction(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final var assignee = (String) variables.get("assignee");

    final var currentPlayer = (String) variables.get("nextPlayer");
    final var players = (Map<String, List<String>>) variables.get("players");
    final var currentHand = players.get(currentPlayer);

    final var playNope = ThreadLocalRandom.current().nextDouble() > 0.5;

    if (currentHand.contains("nope") && !assignee.equals(currentPlayer) && playNope) {

      final var cards = (List<String>) variables.get("cards");
      cards.add("nope");

      log.info("Player {} plays a 'nope' card", assignee);

      jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of(
          "cards", cards)).send();
    }
  }
}
