package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.concurrent.ThreadLocalRandom;

public class SelectOtherPlayer implements JobHandler {

  private final GameListener listener;

  public SelectOtherPlayer(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();

    final var playerNames = variables.getPlayerNames();
    playerNames.remove(currentPlayer);

    final var index = ThreadLocalRandom.current().nextInt(0, playerNames.size());
    final var otherPlayer = playerNames.get(index);

    listener.playerToDrawSelected(GameContext.of(job), currentPlayer, otherPlayer);

    variables.putOtherPlayer(otherPlayer);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
