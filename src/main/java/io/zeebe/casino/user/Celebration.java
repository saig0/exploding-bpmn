package io.zeebe.casino.user;

import com.google.common.base.Strings;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;

public class Celebration implements JobHandler {

  private final Logger log;

  public Celebration(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final Map players = (Map) variables.get("players");
    final String winner = new ArrayList<>((Set<String>) players.keySet()).get(0);

    final int count = 16 - winner.length();
    log.info("\n"
        + "=========================================="
        + "\n=============WINNER: {} " + Strings.repeat("=", count > 0 ? count : 0)
        + "\n==========================================", players.keySet());

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("winner", winner)).send();
  }
}
