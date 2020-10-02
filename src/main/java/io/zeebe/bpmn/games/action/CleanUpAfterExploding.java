package io.zeebe.bpmn.games.action;

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
public class CleanUpAfterExploding implements JobHandler {

  private final GameListener listener;

  @Autowired
  public CleanUpAfterExploding(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "cleanUpAfterExploding")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var discardPile = variables.getDiscardPile();

    final var currentPlayer = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var playerNames = variables.getPlayerNames();

    playerNames.remove(currentPlayer);

    final var handCards = players.remove(currentPlayer);
    discardPile.addAll(handCards);

    final var nextPlayerIndex = Math.max(variables.getNextPlayerIndex() - 1, 0);

    listener.playerExploded(GameContext.of(job), currentPlayer);

    variables
        .putPlayers(players)
        .putPlayerNames(playerNames)
        .putNextPlayerIndex(nextPlayerIndex)
        .putDiscardPile(discardPile)
        .putTurns(0)
        .putPlayerCount(players.size());
    // card = null ?

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
