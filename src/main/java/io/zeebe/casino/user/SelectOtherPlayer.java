package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class SelectOtherPlayer implements JobHandler {

  private final Logger log;

  public SelectOtherPlayer(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();

    final String currentPlayer = variables.get("nextPlayer").toString();
    final Map players = (Map) variables.get("players");

    players.keySet().remove(currentPlayer);

    final int index = ThreadLocalRandom.current().nextInt(0, players.size());
    final String otherPlayer = new ArrayList<String>((Set<String>) players.keySet()).get(index);

    log.info("Player {} choose player {} to select a random card from.", currentPlayer, otherPlayer);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("otherPlayer", otherPlayer));
  }
}
