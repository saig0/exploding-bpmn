package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;

public class InjectExplodingKitten implements JobHandler {

  private final Logger log;

  public InjectExplodingKitten(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final var deck = (List<String>) variables.get("deck");

    // TODO select position by player
    final var position = new Random().nextInt(deck.size());

    deck.add(position, "exploding");

    log.info("Insert exploding kitten into deck at position {}", position);

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("deck", deck)).send();
  }
}
