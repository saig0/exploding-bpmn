package io.zeebe.bpmn.games.deck;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;

public class InitGame implements JobHandler {

  private final Logger log;

  public InitGame(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {

    final var playerNames =
        Optional.ofNullable((List<String>) job.getVariablesAsMap().get("playerNames"))
            .orElseThrow(() -> new RuntimeException("no player names passed as 'playerNames'"));

    final var playerCount = playerNames.size();
    if (playerCount < 2 || playerCount > 10) {
      throw new RuntimeException(
          String.format("Expected between 2 and 10 players but was {}", playerCount));
    }

    log.info("Starting game with players: {}", playerNames);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(Map.of(
            "round",0,
            "turns",1,
            "correlationKey", String.valueOf(job.getWorkflowInstanceKey()),
            "allPlayers", playerNames))
        .send()
        .join();
  }
}
