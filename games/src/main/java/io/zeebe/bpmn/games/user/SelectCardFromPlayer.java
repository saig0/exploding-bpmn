package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.concurrent.ThreadLocalRandom;

public class SelectCardFromPlayer implements JobHandler {

  private final GameListener listener;

  public SelectCardFromPlayer(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var otherPlayer = variables.getOtherPlayer();
    final var players = variables.getPlayers();
    final var otherHand = players.get(otherPlayer);

    if (!otherHand.isEmpty()) {
      final int randomCardIndex = ThreadLocalRandom.current().nextInt(0, otherHand.size());
      final var card = otherHand.get(randomCardIndex);

      listener.cardChosenFrom(GameContext.of(job), currentPlayer, otherPlayer, card);

      variables.putCard(card);
    }

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
