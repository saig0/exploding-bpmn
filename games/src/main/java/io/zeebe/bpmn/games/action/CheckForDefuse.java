package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;

public class CheckForDefuse implements JobHandler {

  private final GameListener listener;

  public CheckForDefuse(GameListener listener) {
    this.listener = listener;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(currentPlayer);

    final var defuseCard = hand.stream().filter(c -> c.getType() == CardType.DEFUSE).findFirst();
    final var hasDefuseCard = defuseCard.isPresent();

    listener.handCheckedForDefuse(GameContext.of(job), currentPlayer, hand);

    variables
        .putCards(defuseCard.map(List::of).orElse(List.of()))
        .putHasDefuse(hasDefuseCard);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
