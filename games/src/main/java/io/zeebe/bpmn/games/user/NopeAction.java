package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;

public class NopeAction implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  public NopeAction(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var playedCards = variables.getLastPlayedCards();

    final var currentPlayer = variables.getNextPlayer();
    final var nopePlayer = variables.getNopePlayer();

    final var players = variables.getPlayers();
    final var playersHand = players.get(nopePlayer);

    final var nopeCard = playersHand.stream().filter(c -> c.getType() == CardType.NOPE).findFirst();

    // final var wantToNope = ThreadLocalRandom.current().nextDouble() > 0.5;

    if (!nopePlayer.equals(currentPlayer) && nopeCard.isPresent()) {

      interaction
          .nopeThePlayedCard(nopePlayer)
          .thenAccept(
              wantToNope -> {
                if (wantToNope) {
                  completeJob(jobClient, job, playedCards, nopePlayer);
                }
              });
    }
  }

  private void completeJob(
      JobClient jobClient, ActivatedJob job, List<Card> playedCards, String nopePlayer) {

    listener.playerNoped(GameContext.of(job), nopePlayer, playedCards);

    jobClient.newCompleteCommand(job.getKey()).send().join();
  }
}
