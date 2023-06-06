package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.PlayerTurn;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SelectAction implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public SelectAction(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "selectAction")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);
    final var playerTurn = PlayerTurn.of(variables);

    interaction
        .selectCardsToPlay(playerTurn)
        .thenAccept(
            cardsToPlay ->
                completeJob(jobClient, job, variables, playerTurn.getCurrentPlayer(), cardsToPlay));
  }

  private void completeJob(
      JobClient jobClient,
      ActivatedJob job,
      Variables variables,
      String player,
      List<Card> cardsToPlay) {

    variables.putCards(cardsToPlay).putLastPlayedCards(cardsToPlay);

    if (cardsToPlay.isEmpty()) {
      variables.putAction("pass");
      listener.playerPassed(GameContext.of(job), player);

    } else {

      final var card = cardsToPlay.get(0).getType();
      final var action = card.isCatCard() ? "cat-pair" : card.name().toLowerCase();

      variables.putAction(action);
      listener.cardsPlayed(GameContext.of(job), player, cardsToPlay);
    }

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
