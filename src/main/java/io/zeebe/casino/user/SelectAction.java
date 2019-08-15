package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
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

    final boolean shouldPass = ThreadLocalRandom.current().nextDouble() > 0.25;
    if (shouldPass)
    {
      log.info("Player {} passes.", nextPlayer);

      jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("action", "")).send();
    }
    else
    {
      final Map players = (Map) variables.get("players");

      final List<String> hand = (List<String>) players.get(nextPlayer);
// CAT algo ?
//      final Map<String, List<String>> cardGroups = hand.stream()
//          .filter(card -> card.contains("cat"))
//          .collect(Collectors.groupingBy(card -> card));
//
//
//      if (cardGroups.size() > 2)
//      {
//        if (cardGroups.containsKey("feral-cat"))
//        {
//
//          final int feralCount = cardGroups.get("feral-cat").size();
//
//        }
//      }
//      else
//      {
//        //
//      }
//
//
//
//      final long catCount = hand.stream().filter(card -> card.contains("cat")).count();

      final List<String> handWithoutDefuse = hand.stream().filter(card -> !card.equals("defuse") && !card.equals("nope"))
//          .filter(card -> catCount >= 2 ? true : !card.contains("cat"))
          .collect(Collectors.toList());

      final int handSize = handWithoutDefuse.size();

      if (handSize == 0)
      {
        // pass
        jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("action", "")).send();
        return;
      }

      final int index = ThreadLocalRandom.current().nextInt(0, handSize);
      final String card = handWithoutDefuse.remove(index);
      hand.remove(card);

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
