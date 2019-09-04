package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NopeAction implements JobHandler {

  private final GameListener listener;

  public NopeAction(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var playedCards = variables.getLastPlayedCards();

    final var currentPlayer = variables.getNextPlayer();
    final var nopePlayer = variables.getNopePlayer();

    final var players = variables.getPlayers();
    final var playersHand = players.get(nopePlayer);

    final var nopeCard = playersHand
        .stream()
        .filter(c -> c.getType() == CardType.NOPE)
        .findFirst();

    final var wantToNope = ThreadLocalRandom.current().nextDouble() > 0.5;

    if (!nopePlayer.equals(currentPlayer) && nopeCard.isPresent() && wantToNope) {

      listener.playerNoped(nopePlayer, playedCards);

      jobClient
          .newCompleteCommand(job.getKey())
          .send()
          .join();
    }
  }
}
