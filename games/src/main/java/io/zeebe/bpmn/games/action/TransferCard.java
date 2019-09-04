package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

public class TransferCard implements JobHandler {

  private final GameListener listener;

  public TransferCard(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var otherPlayer = variables.getOtherPlayer();

    final var players = variables.getPlayers();
    final var playersHand = players.get(currentPlayer);
    final var otherHand = players.get(otherPlayer);

    if (!otherHand.isEmpty()) {
      final var card = variables.getCard();

      otherHand.remove(card);
      playersHand.add(card);

      listener.cardTakenFrom(currentPlayer, otherPlayer, card);
    }

    variables.putPlayers(players);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
