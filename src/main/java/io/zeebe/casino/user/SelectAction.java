package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class SelectAction implements JobHandler {


  private final Logger log;

  public SelectAction(Logger log) {
    this.log = log;
  }


  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final String nextPlayer = variables.get("nextPlayer").toString();

    final boolean shouldPass = ThreadLocalRandom.current().nextBoolean();
    if (shouldPass)
    {
      log.info("Player {} passes.", nextPlayer);

      jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("action", "")).send();
    }
    else
    {
      final Map players = (Map) variables.get("players");

      final List<String> hand = (List<String>) players.get(nextPlayer);

      final int handSize = hand.size();
      final int index = ThreadLocalRandom.current().nextInt(0, handSize);
      final String card = hand.remove(index);

      log.info("Player {} picked card {} to play", nextPlayer, card);

      players.put(nextPlayer, hand);
      jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of(
          "players", players,
          "cards", List.of(card),
          "action", card
      )).send();
    }
  }
}
