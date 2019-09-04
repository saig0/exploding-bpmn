package io.zeebe.bpmn.games.deck;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;

public class DiscardNope implements JobHandler {

  private final GameListener listener;

  public DiscardNope(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var discardPile = variables.getDiscardPile();

    final var nopePlayer = variables.getNopePlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(nopePlayer);

    final var nopeCard = hand
        .stream()
        .filter(c -> c.getType() == CardType.NOPE)
        .findFirst()
        .orElseThrow();

    hand.remove(nopeCard);

    discardPile.add(nopeCard);

    listener.cardsDiscarded(nopePlayer, List.of(nopeCard));

    variables
        .putPlayers(players)
        .putDiscardPile(discardPile);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
