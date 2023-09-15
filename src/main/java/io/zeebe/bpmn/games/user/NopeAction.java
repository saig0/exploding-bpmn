package io.zeebe.bpmn.games.user;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameInteraction;
import io.zeebe.bpmn.games.model.*;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NopeAction implements JobHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(NopeAction.class);

  private final GameInteraction interaction;

  @Autowired
  public NopeAction(GameInteraction interaction) {
    this.interaction = interaction;
  }

  @ZeebeWorker(type = "play-nope")
  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);
    final var nopeTurn = NopeTurn.of(variables);

    final var currentPlayer = nopeTurn.getCurrentPlayer();
    final var nopePlayer = variables.getNopePlayer();
    final var nopedPlayer = Optional.ofNullable(variables.getNopedPlayer());

    final var nopeCard =
        nopeTurn.getHandCards().stream().filter(c -> c.getType() == CardType.NOPE).findFirst();

    // don't nope your self
    // don't nope if you played the last nope

    final boolean hasPlayedNopeOrCards = nopePlayer.equals(nopedPlayer.orElse(currentPlayer));
    final boolean hasNopeCard = nopeCard.isPresent();

    LOGGER.debug(
        "Nope action for {}? [has-played-nope-or-cards: {}, has-nope-card: {}]",
        nopePlayer,
        hasPlayedNopeOrCards,
        hasNopeCard);

    if (!hasPlayedNopeOrCards && hasNopeCard) {

      LOGGER.debug("Nope action: {} want to nope?", nopePlayer);

      interaction
          .nopeThePlayedCard(nopeTurn)
          .thenAccept(
              wantToNope -> {
                if (wantToNope) {
                  jobClient.newCompleteCommand(job.getKey()).send().join();
                }
              });
    }
  }
}
