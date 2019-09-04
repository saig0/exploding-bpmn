package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class CleanUpAfterExploding implements JobHandler {

  private final GameListener listener;

  public CleanUpAfterExploding(GameListener listener) {
    this.listener = listener;
  }

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

    listener.playerExploded(currentPlayer);

    variables
        .putPlayers(players)
        .putPlayerNames(playerNames)
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
