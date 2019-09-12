package io.zeebe.bpmn.games.user;

import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;

public class SelectAction implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  public SelectAction(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var player = variables.getNextPlayer();
    final var players = variables.getPlayers();
    final var hand = players.get(player);

    final var deck = variables.getDeck();

    interaction
        .selectCardsToPlay(player, hand, deck.size())
        .thenAccept(cardsToPlay -> completeJob(jobClient, job, variables, player, cardsToPlay));
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
