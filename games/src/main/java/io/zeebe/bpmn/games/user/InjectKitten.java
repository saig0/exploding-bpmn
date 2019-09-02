package io.zeebe.bpmn.games.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class InjectKitten implements JobHandler {

  private final Logger log;

  public InjectKitten(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();
    final var card = variables.remove("card").toString();
    final var deck = (List<String>) variables.get("deck");

    final var currentPlayer = (String) variables.get("nextPlayer");
    log.info("Remove card {} from player {}'s hand", card, currentPlayer);

    final var players = (Map<String, List<String>>) variables.get("players");
    final var handCards = players.get(currentPlayer);
    handCards.remove(card);
    players.put(currentPlayer, handCards);

    final int index = deck.size() > 0 ? ThreadLocalRandom.current().nextInt(0, deck.size()) : 0;
    deck.add(index, card);

    log.info("Exploding was inserted again in the deck at position {}", index);

    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(
            Map.of(
                "deck", deck,
                "players", players))
        .send();
  }
}
