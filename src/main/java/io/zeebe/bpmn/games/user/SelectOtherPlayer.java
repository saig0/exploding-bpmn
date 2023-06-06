package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.PlayersOverview;
import io.zeebe.bpmn.games.model.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SelectOtherPlayer implements JobHandler {

  private static Logger LOGGER = LoggerFactory.getLogger(SelectOtherPlayer.class);

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public SelectOtherPlayer(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "selectOtherPlayer")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);
    final var playersOverview = PlayersOverview.of(variables);

    final var currentPlayer = variables.getNextPlayer();
    final var otherPlayers = playersOverview.getPlayers();

    if (otherPlayers.size() == 1) {
      final var otherPlayer = otherPlayers.get(0).getName();
      completeJob(jobClient, job, variables, currentPlayer, otherPlayer);
    } else {
      LOGGER.debug("Select one player of {}", playersOverview.getPlayers());

      interaction
          .selectPlayer(currentPlayer, playersOverview)
          .thenAccept(
              otherPlayer -> completeJob(jobClient, job, variables, currentPlayer, otherPlayer));
    }
  }

  private void completeJob(
      JobClient jobClient,
      ActivatedJob job,
      Variables variables,
      String currentPlayer,
      String otherPlayer) {

    listener.playerToDrawSelected(GameContext.of(job), currentPlayer, otherPlayer);

    variables.putOtherPlayer(otherPlayer);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
