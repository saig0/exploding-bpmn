package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class Celebration implements JobHandler {

  private final GameListener listener;

  public Celebration(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var playerNames = variables.getPlayerNames();
    final var winner = playerNames.get(0);

    listener.playerWonTheGame(GameContext.of(job), winner);

    jobClient.newCompleteCommand(job.getKey())
        .send()
        .join();
  }
}
