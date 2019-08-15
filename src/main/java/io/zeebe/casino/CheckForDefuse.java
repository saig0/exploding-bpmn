package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class CheckForDefuse implements JobHandler {

  private final Logger log;

  public CheckForDefuse(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final String currentPlayer = variables.get("nextPlayer").toString();
    final Map players = (Map) variables.get("players");
    final var hand = (List<String>) players.get(currentPlayer);

    final var result = new HashMap<String, Object>();

    if (hand.remove("defuse")) {
      log.info("Player {} was able to defuse exploding kitten.", currentPlayer);
      result.put("cards", List.of("defuse"));
      players.put(currentPlayer, hand);
      result.put("players", players);

      result.put("defuse", true);
    }

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(result).send();
  }
}
