package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.GameState;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectPlayer implements JobHandler {

  private final GameListener listener;

  public SelectPlayer(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();

    final var players = (Map<String, List<Card>>) variables.get("players");

    final int round = (int) variables.get("round");


    final var playerNames = new ArrayList<>(players.keySet());
    final var nextPlayer = playerNames.get(round % playerNames.size());

    final int turns = (int) variables.get("turns");
    final var turnArray = new int[turns];

    listener.nextPlayerSelected(nextPlayer, turns);

    jobClient
        .newCompleteCommand(activatedJob.getKey())
        .variables(
            Map.of(
                "round", round + 1,
                "nextPlayer", nextPlayer,
                "turnArray", turnArray))
        .send();
  }
}
