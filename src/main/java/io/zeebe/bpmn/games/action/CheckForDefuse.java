package io.zeebe.bpmn.games.action;

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
public class CheckForDefuse implements JobHandler {

  private final GameListener listener;

  @Autowired
  public CheckForDefuse(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "checkForDefuse")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(currentPlayer);

    final var defuseCard = hand.stream().filter(c -> c.getType() == CardType.DEFUSE).findFirst();
    final var hasDefuseCard = defuseCard.isPresent();

    listener.handCheckedForDefuse(GameContext.of(job), currentPlayer, hand);

    variables.putCards(defuseCard.map(List::of).orElse(List.of())).putHasDefuse(hasDefuseCard);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
