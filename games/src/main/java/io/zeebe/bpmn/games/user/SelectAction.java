package io.zeebe.bpmn.games.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

      jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of(
          "cards", Collections.emptyList(),
          "action", "pass")).send();
    }
    else
    {
      final Map players = (Map) variables.get("players");
      final List<String> hand = (List<String>) players.get(nextPlayer);

      final Map<String, List<String>> catCards =
          hand.stream()
              .filter(card -> card.contains("cat"))
              .collect(Collectors.groupingBy(card -> card));

      final var twoSameCatCards = catCards
          .entrySet()
          .stream()
          .filter(e -> e.getValue().size() >= 2)
          .map(e -> e.getValue().subList(0, 2))
          .findFirst();

      final var catCard = catCards.keySet().stream().filter(card -> !card.equals("feral-cat"))
          .findFirst();

      final var feralAndOtherCat = Optional.of(catCards)
          .filter(cards -> cards.size() >= 2)
          .filter(cards -> cards.containsKey("feral-cat"))
          .map(cards -> List.of("feral-cat", catCard.get()));

      final var actionCards = hand
          .stream()
          .filter(card -> !List.of("nope", "defuse").contains(card))
          .filter(card -> !card.contains("cat"))
          .collect(Collectors.toList());

      final var actionCard = Optional.of(actionCards)
          .filter(cards -> !cards.isEmpty())
          .map(cards -> {
            final var cardIdx = ThreadLocalRandom.current().nextInt(0, cards.size());
            return List.of(cards.get(cardIdx));
          });

      final Map<String, Object> cards = twoSameCatCards
          .or(() -> feralAndOtherCat)
          .or(() -> actionCard)
          .map(cardsToPlay -> Map.of(
              "cards", cardsToPlay,
              "action", cardsToPlay.get(0)))
          .orElse(Map.of(
              "cards", Collections.emptyList(),
              "action", "pass"));

      log.info("Player {} picked card(s) '{}' to play", nextPlayer, cards.get("cards"));

      jobClient.newCompleteCommand(activatedJob.getKey()).variables(cards).send();
    }
  }
}
