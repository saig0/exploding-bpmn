package io.zeebe.casino.user;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;

public class SelectAction implements JobHandler {


  private final Logger log;

  public SelectAction(Logger log) {
    this.log = log;
  }


  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();

    final String nextPlayer = variables.get("nextPlayer").toString();
    final Map players = (Map) variables.get("players");

    final List<String> hand = (List<String>) players.get("hand");

    final int index = ThreadLocalRandom.current().nextInt(0, hand.size());
    final String card = hand.remove(index);

    log.info("Picked card {} to play", card);

    players.put("hand", hand);
    variables.put("players", players);

    variables.put("cards", List.of(card));
    variables.put("action", card);
  }
}
