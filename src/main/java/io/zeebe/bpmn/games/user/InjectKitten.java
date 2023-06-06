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
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InjectKitten implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public InjectKitten(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "injectKitten")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) throws Exception {
    final var variables = Variables.from(job);
    final var playerTurn = PlayerTurn.of(variables);

    final var card = variables.getCard();
    final var deck = variables.getDeck();

    final var currentPlayer = playerTurn.getCurrentPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(currentPlayer);

    hand.remove(card);

    if (deck.isEmpty()) {
      deck.add(card);

      completeJob(jobClient, job, variables, card, deck, currentPlayer, players);

    } else {

      interaction
          .selectPositionToInsertExplodingCard(playerTurn, card)
          .thenAccept(
              index -> {
                deck.add(index, card);

                completeJob(jobClient, job, variables, card, deck, currentPlayer, players);
              });
    }
  }

  private void completeJob(
      JobClient jobClient,
      ActivatedJob job,
      Variables variables,
      Card card,
      List<Card> deck,
      String currentPlayer,
      Map<String, List<Card>> players) {

    listener.playerInsertedCard(GameContext.of(job), currentPlayer, card, deck);

    variables.putPlayers(players).putDeck(deck);

    jobClient
        .newCompleteCommand(job.getKey())
        .variables(variables.getResultVariables())
        .send()
        .join();
  }
}
