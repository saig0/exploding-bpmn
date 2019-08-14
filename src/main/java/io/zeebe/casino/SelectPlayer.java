package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Map;

public class SelectPlayer implements JobHandler {


  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final int round = (int) variables.get("round");
    final String[] players = (String[]) variables.get("players");
    final var nextPlayer = players[round % players.length];

    final int turns = (int) variables.get("turns");
    final var turnArray = new int[turns];

    jobClient.newCompleteCommand(activatedJob.getKey())
        .variables(Map.of("round", round + 1,
            "nextPlayer", nextPlayer,
            "turnArray", turnArray)).send();

  }
}
