package io.zeebe.bpmn.games.deck;

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
public class DrawBottomCard implements JobHandler {

  private final GameListener listener;

  @Autowired
  public DrawBottomCard(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "drawBottomCard")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);

    final var deck = variables.getDeck();
    final var card = deck.remove(deck.size() - 1);

    final var currentPlayer = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var handCards = players.get(currentPlayer);

    handCards.add(card);

    listener.playerDrawnCardFromBottom(GameContext.of(job), currentPlayer, card);

    variables.putPlayers(players).putDeck(deck).putCard(card);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
