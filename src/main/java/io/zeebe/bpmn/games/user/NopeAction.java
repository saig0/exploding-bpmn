package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.model.Card;
import io.zeebe.bpmn.games.model.CardType;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NopeAction implements JobHandler {

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public NopeAction(GameListener listener, GameInteraction interaction) {
    this.listener = listener;
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "play-nope")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var currentPlayer = variables.getNextPlayer();
    final var nopePlayer = variables.getNopePlayer();
    final var nopedPlayer = Optional.ofNullable(variables.getNopedPlayer());

    final var players = variables.getPlayers();
    final var playersHand = players.get(nopePlayer);

    final var nopeCard = playersHand.stream().filter(c -> c.getType() == CardType.NOPE).findFirst();

    // don't nope your self
    // don't nope if you played the last nope

    if (!nopePlayer.equals(nopedPlayer.orElse(currentPlayer)) && nopeCard.isPresent()) {

      interaction
          .nopeThePlayedCard(nopePlayer)
          .thenAccept(
              wantToNope -> {
                if (wantToNope) {
                  jobClient.newCompleteCommand(job.getKey()).send().join();
                }
              });
    }
  }
}
