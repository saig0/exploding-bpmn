package io.zeebe.casino;

import io.zeebe.casino.action.AlterAction;
import io.zeebe.casino.action.AttackAction;
import io.zeebe.casino.action.DrawAction;
import io.zeebe.casino.action.SeeAction;
import io.zeebe.casino.action.SkipAction;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {

    final var zeebeClient =
        ZeebeClient.newClientBuilder()
            .brokerContactPoint("192.168.30.188:26500")
            .usePlaintext()
            .build();

    // ---
    LOG.info("> deploying workflows");
    zeebeClient.newDeployCommand().addResourceFromClasspath("explodingKittens.bpmn").send().join();

    // ---
    LOG.info("> start demo");
    zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("Process_1")
        .latestVersion()
        .variables(
            Map.of(
                "playerNames",
                List.of("phil", "chris"),
                "round",
                0,
                "turns",
                1,
                "correlationKey",
                UUID.randomUUID()))
        .send()
        .join();

    // general
    installWorkers(zeebeClient,
        Map.of("build-deck", new BuildDeck(LOG),
            "selectPlayerForNewRound", new SelectPlayer(LOG),
            "selectAction", new SelectAction(LOG),
            "discardCard", new DiscardCard(LOG),
            "addTurns", new AddTurns(LOG),
            "drawBottomCard", new DrawBottomCard(LOG),
            "drawTopCard", new DrawTopCard(LOG)));

    // actions
    installWorkers(zeebeClient,
        Map.of("skip", new SkipAction(LOG),
            "attack", new AttackAction(LOG),
//            "favor", new FavorAction(LOG),
//            "cats", new CatsAction(LOG),
            "see", new SeeAction(LOG),
            "alter", new AlterAction(LOG),
            "draw", new DrawAction(LOG)));

  }

  private static void installWorkers(ZeebeClient zeebeClient,
      Map<String, JobHandler> jobTypeHandlers) {
    for (var jobTypeHandler : jobTypeHandlers.entrySet()) {
      zeebeClient.newWorker()
          .jobType(jobTypeHandler.getKey())
          .handler(jobTypeHandler.getValue())
          .open();
    }
  }
}
