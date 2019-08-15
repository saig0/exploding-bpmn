package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
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

    final boolean defused = hand.remove("defuse");
    if (defused)
    {
      variables.put("cards", List.of("defuse"));
      players.put(currentPlayer, hand);
      variables.put("players", players);
    }


    log.info("Player {} was {}able to defuse exploding kitten.", currentPlayer, (defused ? "" : "not "));
    variables.put("defuse", defused);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(variables).send();
  }
}
