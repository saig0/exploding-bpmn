package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;

public class BuildDeck implements JobHandler {

  private final Logger log;

  public BuildDeck(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {

    final List<String> playerNames = (List<String>) job.getVariablesAsMap().get("playerNames");
    final var playerCount = playerNames.size();

    int times;
    if (playerCount < 4) {
      times = 0;
    } else if (playerCount < 8) {
      times = 1;
    } else {
      times = 2;
    }

    var actionCards =
        Map.of(
            "attack", List.of(4, 7, 11),
            "skip", List.of(4, 6, 10),
            "see-the-future", List.of(3, 3, 6),
            "alter-the-future", List.of(2, 4, 6),
            "shuffle", List.of(2, 4, 6),
            "draw-from-bottom", List.of(3, 4, 7),
            "favor", List.of(2, 4, 6),
            "nope", List.of(4, 6, 10));
    var catCards =
        Map.of(
            "feral-cat", List.of(2, 4, 6),
            "cat-1", List.of(3, 4, 7),
            "cat-2", List.of(3, 4, 7),
            "cat-3", List.of(3, 4, 7),
            "cat-4", List.of(3, 4, 7),
            "cat-5", List.of(3, 4, 7));

    var cards = new HashMap<String, List<Integer>>();
    cards.putAll(actionCards);
    cards.putAll(catCards);

    final var deck =
        cards.entrySet().stream()
            .flatMap(
                entry -> Collections.nCopies(entry.getValue().get(times), entry.getKey()).stream())
            .collect(Collectors.toList());

    Collections.shuffle(deck);

    final Map<String, List<String>> players =
        playerNames.stream()
              .collect(Collectors.toMap(name -> name, p -> {
                var hand =
                    IntStream.range(0, 7)
                        .mapToObj(i -> deck.remove(0))
                        .collect(Collectors.toList());

                hand.add("defuse");
                return hand;
              }));

    deck.addAll(Collections.nCopies(playerCount - 1, "exploding"));

    int defuseCards = -playerCount;
    if (playerCount < 4) {
      defuseCards += 3;
    } else if (playerCount < 8) {
      defuseCards += 7;
    } else {
      defuseCards += 10;
    }

    if (defuseCards > 0) {
      deck.addAll(Collections.nCopies(defuseCards, "defuse"));
    }

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(Map.of(
            "deck", deck,
            "players", players))
        .send()
        .join();
  }
}
