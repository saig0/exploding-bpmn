package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class SelectPlayer implements JobHandler {

  private final Logger log;

  public SelectPlayer(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final int round = (int) variables.get("round");
    final List<String> players = (List<String>) variables.get("players");
    final var nextPlayer = players.get(round % players.size());
    log.info("Choosed next player {}.", nextPlayer);

    final int turns = (int) variables.get("turns");
    final var turnArray = new int[turns];

    jobClient.newCompleteCommand(activatedJob.getKey())
        .variables(Map.of("round", round + 1,
            "nextPlayer", nextPlayer,
            "turnArray", turnArray)).send();

  }
}
