package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShowTopThree implements JobHandler {

  private final GameListener listener;

  @Autowired
  public ShowTopThree(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "showTopThreeCards")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var deck = variables.getDeck();

    final var amount = Math.min(3, deck.size());
    final var cards = deck.subList(0, amount);

    listener.playerSawTheFuture(GameContext.of(job), currentPlayer, cards);

    jobClient.newCompleteCommand(job.getKey()).send().join();
  }
}
