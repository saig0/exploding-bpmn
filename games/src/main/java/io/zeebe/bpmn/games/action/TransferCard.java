package io.zeebe.bpmn.games.action;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

public class TransferCard implements JobHandler {

  private final Logger log;

  public TransferCard(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
    final var variables = activatedJob.getVariablesAsMap();

    final String currentPlayer = variables.get("nextPlayer").toString();
    final String otherPlayer = variables.get("otherPlayer").toString();
    final Map players = (Map) variables.get("players");

    final var currentHand = (List<String>) players.get(currentPlayer);
    final var otherHand = (List<String>) players.get(otherPlayer);

    final int index = (int) variables.get("choosenCard");
    if (index >= 0)
    {
      final String choosenCard = otherHand.remove(index);

      log.info("Transfer card {} from player {} to player {}", choosenCard, otherPlayer, currentPlayer);
      currentHand.add(choosenCard);
      players.put(currentPlayer, currentHand);
      players.put(otherPlayer, otherHand);
    }

    jobClient.newCompleteCommand(activatedJob.getKey()).variables(Map.of("players", players)).send();
  }
}
