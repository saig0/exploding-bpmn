package io.zeebe.bpmn.games;

import io.zeebe.bpmn.games.action.AddTurns;
import io.zeebe.bpmn.games.action.CheckForDefuse;
import io.zeebe.bpmn.games.action.CleanUpAfterExploding;
import io.zeebe.bpmn.games.action.EndTurn;
import io.zeebe.bpmn.games.action.NewTurn;
import io.zeebe.bpmn.games.action.SelectPlayer;
import io.zeebe.bpmn.games.action.ThrowMessage;
import io.zeebe.bpmn.games.action.TransferCard;
import io.zeebe.bpmn.games.action.UpdateDeck;
import io.zeebe.bpmn.games.deck.BuildDeck;
import io.zeebe.bpmn.games.deck.DiscardCards;
import io.zeebe.bpmn.games.deck.DiscardNope;
import io.zeebe.bpmn.games.deck.DrawBottomCard;
import io.zeebe.bpmn.games.deck.DrawTopCard;
import io.zeebe.bpmn.games.deck.InitGame;
import io.zeebe.bpmn.games.user.Celebration;
import io.zeebe.bpmn.games.user.ChangeOrder;
import io.zeebe.bpmn.games.user.InjectKitten;
import io.zeebe.bpmn.games.user.NopeAction;
import io.zeebe.bpmn.games.user.SelectAction;
import io.zeebe.bpmn.games.user.SelectCardFromPlayer;
import io.zeebe.bpmn.games.user.SelectOtherPlayer;
import io.zeebe.bpmn.games.user.SelectRandomCard;
import io.zeebe.bpmn.games.user.ShowTopThree;
import io.zeebe.bpmn.games.user.ShuffleDeck;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.worker.JobHandler;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GamesApplication {

  private static final Logger LOG = LoggerFactory.getLogger(GamesApplication.class);

  private final ZeebeClient client;
  private final GameListener listener;

  public GamesApplication(ZeebeClient client, GameListener listener) {
    this.client = client;
    this.listener = listener;
  }

  public void start() {
    LOG.info("Deploy workflows");

    client.newDeployCommand().addResourceFromClasspath("explodingKittens.bpmn").send().join();

    LOG.info("Start workers");

    // general
    installWorkers(
        Map.of(
            "initGame", new InitGame(LOG),
            "build-deck", new BuildDeck(listener),
            "selectPlayerForNewRound", new SelectPlayer(listener),
            "discard", new DiscardCards(LOG),
            "discardNope", new DiscardNope(LOG),
            "addTurns", new AddTurns(LOG),
            "endTurn", new EndTurn(LOG),
            "newTurn", new NewTurn(LOG),
            "checkForDefuse", new CheckForDefuse(LOG)));

    // deck based
    installWorkers(
        Map.of(
            "build-deck",
            new BuildDeck(listener),
            "discard",
            new DiscardCards(LOG),
            "cleanUpAfterExploding",
            new CleanUpAfterExploding(LOG),
            "drawBottomCard",
            new DrawBottomCard(LOG),
            "drawTopCard",
            new DrawTopCard(LOG),
            "injectKitten",
            new InjectKitten(LOG)));

    // actions
    installWorkers(
        Map.of(
            "updateDeck", new UpdateDeck(LOG),
            "transferCard", new TransferCard(LOG),
            "shuffle", new ShuffleDeck(LOG)));

    installWorkers(Map.of("throwMessage", new ThrowMessage(client)));

    // user
    installWorkers(
        Map.of(
            "showTopThreeCards", new ShowTopThree(LOG),
            "changeOrder", new ChangeOrder(LOG),
            "selectAction", new SelectAction(LOG),
            "selectOtherPlayer", new SelectOtherPlayer(LOG),
            "selectCardFromPlayer", new SelectCardFromPlayer(LOG),
            "chooseRandomCard", new SelectRandomCard(LOG),
            "celebrate", new Celebration(LOG),
            "play-nope", new NopeAction(LOG)));
  }

  public void startNewGame(Collection<String> playerNames) {
    client
        .newCreateInstanceCommand()
        .bpmnProcessId("exploding-kittens")
        .latestVersion()
        .variables(Map.of("playerNames", playerNames))
        .send()
        .join();
  }

  private void installWorkers(Map<String, JobHandler> jobTypeHandlers) {
    for (var jobTypeHandler : jobTypeHandlers.entrySet()) {
      client
          .newWorker()
          .jobType(jobTypeHandler.getKey())
          .handler(jobTypeHandler.getValue())
          .timeout(Duration.ofSeconds(5))
          .open();
    }
  }
}
