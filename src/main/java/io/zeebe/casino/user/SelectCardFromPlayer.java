package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class SelectCardFromPlayer implements JobHandler {

  private final Logger log;

  public SelectCardFromPlayer(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();

    final String otherPlayer = variables.get("otherPlayer").toString();
    final Map players = (Map) variables.get("players");

    final var otherHand = (List<String>) players.get(otherPlayer);
    final int index = ThreadLocalRandom.current().nextInt(0, otherHand.size());

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("choosenCard", index)).send();
  }
}
