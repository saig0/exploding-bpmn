package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import org.slf4j.Logger;

public class ShowTopThree implements JobHandler {

  private final Logger log;

  public ShowTopThree(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final var deck = (List<String>) variables.get("deck");
    final String currentPlayer = variables.get("nextPlayer").toString();

    final List<String> topThreeCards = deck.subList(0, 3);

    log.info("Player {} takes a look at the current top three cards on deck {}", currentPlayer, topThreeCards);

    jobClient.newCompleteCommand(activatedJob.getKey()).send();
  }
}
