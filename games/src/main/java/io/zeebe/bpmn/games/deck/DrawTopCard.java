package io.zeebe.bpmn.games.deck;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;

public class DrawTopCard implements JobHandler {

  private final Logger log;

  public DrawTopCard(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final Map<String, Object> variables = drawCard(log, activatedJob, () -> 0);
    jobClient.newCompleteCommand(activatedJob.getKey()).variables(variables).send();
  }

  static Map<String, Object> drawCard(
      Logger log, ActivatedJob activatedJob, Supplier<Integer> drawingCardIndex) {
    final var variables = activatedJob.getVariablesAsMap();

    final var deck = (List<String>) variables.get("deck");

    int index = drawingCardIndex.get();
    if (index == Integer.MAX_VALUE) {
      index = deck.size() - 1;
    }

    final var card = deck.remove(index);

    final String currentPlayer = (String) variables.get("nextPlayer");
    log.info("Player {} draw card {}", currentPlayer, card);

    final Map players = (Map) variables.get("players");
    final var handCards = (List<String>) players.get(currentPlayer);

    handCards.add(card);
    players.put(currentPlayer, handCards);

    return Map.of(
        "card", card,
        "deck", deck,
        currentPlayer, handCards,
        "players", players);
  }
}
