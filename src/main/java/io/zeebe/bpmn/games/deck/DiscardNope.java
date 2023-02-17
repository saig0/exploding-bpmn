package io.zeebe.bpmn.games.deck;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiscardNope implements JobHandler {

  private final GameListener listener;

  @Autowired
  public DiscardNope(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "discardNope")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var discardPile = variables.getDiscardPile();

    final var nopedPlayer = variables.getNopedPlayer();

    listener.playerNoped(GameContext.of(job), nopedPlayer, variables.getLastPlayedCards());

    final var players = variables.getPlayers();
    final var hand = players.get(nopedPlayer);
    final var nopeCard =
        hand.stream().filter(c -> c.getType() == CardType.NOPE).findFirst().orElseThrow();

    hand.remove(nopeCard);

    discardPile.add(nopeCard);

    listener.cardsDiscarded(GameContext.of(job), nopedPlayer, List.of(nopeCard));

    variables.putPlayers(players).putDiscardPile(discardPile).putLastPlayedCards(List.of(nopeCard));

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
