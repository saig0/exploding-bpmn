package io.zeebe.bpmn.games.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class SelectRandomCard implements JobHandler {

  private final Logger log;

  public SelectRandomCard(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final String currentPlayer = variables.get("nextPlayer").toString();
    final String otherPlayer = variables.get("otherPlayer").toString();
    final Map players = (Map) variables.get("players");

    final var currentHand = (List<String>) players.get(currentPlayer);
    final var otherHand = (List<String>) players.get(otherPlayer);

    if (!otherHand.isEmpty()) {
      final int randomCardIndex = ThreadLocalRandom.current().nextInt(0, otherHand.size());

      final String choosenCard = otherHand.remove(randomCardIndex);
      log.info("Player {} choose card {} from player {}", currentPlayer, choosenCard, otherPlayer);
      currentHand.add(choosenCard);
      players.put(currentPlayer, currentHand);
      players.put(otherPlayer, otherHand);

    }

    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(Map.of("players", players))
        .send();
  }
}
