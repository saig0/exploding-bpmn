package io.zeebe.casino;

import io.zeebe.casino.action.TransferCard;
import io.zeebe.casino.action.UpdateDeck;
import io.zeebe.casino.deck.BuildDeck;
import io.zeebe.casino.deck.DiscardCards;
import io.zeebe.casino.deck.DiscardNope;
import io.zeebe.casino.deck.DrawBottomCard;
import io.zeebe.casino.deck.DrawTopCard;
import io.zeebe.casino.deck.InitGame;
import io.zeebe.casino.user.Celebration;
import io.zeebe.casino.user.ChangeOrder;
import io.zeebe.casino.user.InjectKitten;
import io.zeebe.casino.user.NopeAction;
import io.zeebe.casino.user.SelectAction;
import io.zeebe.casino.user.SelectCardFromPlayer;
import io.zeebe.casino.user.SelectOtherPlayer;
import io.zeebe.casino.user.SelectRandomCard;
import io.zeebe.casino.user.ShowTopThree;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.worker.JobHandler;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {

    final var zeebeClient =
        ZeebeClient.newClientBuilder()
            .brokerContactPoint("192.168.21.185:26500")
            .usePlaintext()
            .build();

    // ---
    LOG.info("> deploying workflows");
    zeebeClient.newDeployCommand().addResourceFromClasspath("explodingKittens.bpmn").send().join();

    // ---
    LOG.info("> starting workers");

    // general
    installWorkers(zeebeClient,
        Map.of(
            "initGame", new InitGame(LOG),
            "build-deck", new BuildDeck(LOG),
            "selectPlayerForNewRound", new SelectPlayer(LOG),
            "discard", new DiscardCards(LOG),
            "discardNope", new DiscardNope(LOG),
            "addTurns", new AddTurns(LOG),
            "endTurn", new EndTurn(LOG),
            "newTurn", new NewTurn(LOG),
            "checkForDefuse", new CheckForDefuse(LOG)));

    // deck based
    installWorkers(zeebeClient,
        Map.of("build-deck", new BuildDeck(LOG),
            "discard", new DiscardCards(LOG),
            "cleanUpAfterExploding", new CleanUpAfterExploding(LOG),
            "drawBottomCard", new DrawBottomCard(LOG),
            "drawTopCard", new DrawTopCard(LOG),
            "injectKitten", new InjectKitten(LOG)));

    // actions
    installWorkers(zeebeClient,
        Map.of(
            "updateDeck", new UpdateDeck(LOG),
            "transferCard", new TransferCard(LOG)
        ));

    installWorkers(zeebeClient, Map.of(
        "throwMessage", new ThrowMessage(zeebeClient)));

    // user
    installWorkers(zeebeClient,
        Map.of(
            "showTopThreeCards", new ShowTopThree(LOG),
            "changeOrder", new ChangeOrder(LOG),
            "selectAction", new SelectAction(LOG),
            "selectOtherPlayer", new SelectOtherPlayer(LOG),
            "selectCardFromPlayer", new SelectCardFromPlayer(LOG),
            "chooseRandomCard", new SelectRandomCard(LOG),
            "celebrate", new Celebration(LOG),
            "play-nope", new NopeAction(LOG)));

    // ---
    LOG.info("> ready!");
  }

  private static void installWorkers(ZeebeClient zeebeClient,
      Map<String, JobHandler> jobTypeHandlers) {
    for (var jobTypeHandler : jobTypeHandlers.entrySet()) {
      zeebeClient.newWorker()
          .jobType(jobTypeHandler.getKey())
          .handler(jobTypeHandler.getValue())
          .timeout(Duration.ofSeconds(30))
          .open();
    }
  }
}
