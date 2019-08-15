package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class DrawTopCard implements JobHandler {

  private final Logger log;

  public DrawTopCard(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final var deck = (List<String>) variables.get("deck");

    final var card = deck.remove(0);
    variables.put("deck", deck);

    final var currentPlayer = variables.get("nextPlayer");
    log.info("Player {} draw card {}", currentPlayer, card);

    final Map players = (Map) variables.get("players");
    final var handCards = (List<String>) players.get(currentPlayer);

    handCards.add(card);
    players.put(currentPlayer, handCards);
    variables.put("players", players);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(variables).send();
  }
}
