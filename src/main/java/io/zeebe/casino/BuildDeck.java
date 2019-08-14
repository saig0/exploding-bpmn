package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class BuildDeck implements JobHandler {

  private final Logger log;

  public BuildDeck(Logger log) {
    this.log = log;
  }


  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {

              final List<String> players = (List<String>) job.getVariablesAsMap().get("players");
              final var playerCount = players.size();

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

              deck.addAll(Collections.nCopies(playerCount - 1, "exploding"));
              deck.addAll(Collections.nCopies(playerCount + 1, "defuse"));

              Collections.shuffle(deck);

//              players.stream().map(player -> {
//                IntStream.range(0, 8).mapToObj(
//                    i -> deck.remove(new Random().nextInt(deck.size()))
//              });

              jobClient
                  .newCompleteCommand(job.getKey())
                  .variables(Map.of("deck", deck))
                  .send()
                  .join();
  }
}
