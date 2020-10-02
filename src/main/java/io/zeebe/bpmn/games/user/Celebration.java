package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Celebration implements JobHandler {

  private final GameListener listener;

  @Autowired
  public Celebration(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "celebrate")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var playerNames = variables.getPlayerNames();
    final var winner = playerNames.get(0);

    listener.playerWonTheGame(GameContext.of(job), winner);

    jobClient.newCompleteCommand(job.getKey()).send().join();
  }
}
