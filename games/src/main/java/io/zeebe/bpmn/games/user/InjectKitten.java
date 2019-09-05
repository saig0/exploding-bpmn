package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.concurrent.ThreadLocalRandom;

public class InjectKitten implements JobHandler {

  private final GameListener listener;

  public InjectKitten(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var card = variables.getCard();
    final var deck = variables.getDeck();

    final var currentPlayer = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(currentPlayer);

    hand.remove(card);

    if (deck.isEmpty()) {
      deck.add(card);
    } else {
      final int index = ThreadLocalRandom.current().nextInt(0, deck.size());
      deck.add(index, card);
    }

    listener.playerInsertedCard(GameContext.of(job), currentPlayer, card, deck);

    variables
        .putPlayers(players)
        .putDeck(deck);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
