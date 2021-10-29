package io.zeebe.bpmn.games;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import io.zeebe.bpmn.games.model.Variables;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ZeebeDeployment(resources = "classpath*:/**/*.bpmn")
public class GamesApplication {

  private static final Logger LOG = LoggerFactory.getLogger(GamesApplication.class);

  private final ZeebeClient client;

  private final GameListener listener;
  private final GameInteraction interaction;

  @Autowired
  public GamesApplication(ZeebeClient client, GameListener listener, GameInteraction interaction) {
    this.client = client;
    this.listener = listener;
    this.interaction = interaction;
  }

  public void start() {
    LOG.info("Deploy workflows");

    client
        .newDeployCommand()
        .addResourceFromClasspath("exploding-kittens.bpmn")
        .addResourceFromClasspath("exploding-kittens-action.bpmn")
        .addResourceFromClasspath("exploding-kittens-nope.bpmn")
        .addResourceFromClasspath("exploding-kittens-turn.bpmn")
        .send()
        .join();
  }

  public String startNewGame(List<String> playerNames, String channelId) {

    final var gameKey = UUID.randomUUID().toString();

    client
        .newCreateInstanceCommand()
        .bpmnProcessId("exploding-kittens")
        .latestVersion()
        .variables(
            Variables.createNew()
                .putPlayerNames(playerNames)
                .putGameKey(gameKey)
                .putChannelId(channelId)
                .getResultVariables())
        .send()
        .join();

    return gameKey;
  }

  private void installWorkers(Map<String, JobHandler> jobTypeHandlers) {
    for (var jobTypeHandler : jobTypeHandlers.entrySet()) {
      client
          .newWorker()
          .jobType(jobTypeHandler.getKey())
          .handler(jobTypeHandler.getValue())
          .timeout(Duration.ofSeconds(10))
          .pollInterval(
              Duration.ofMillis(100).plusMillis(ThreadLocalRandom.current().nextInt(1, 250)))
          // .requestTimeout(Duration.ofSeconds(10).plusSeconds(ThreadLocalRandom.current().nextInt(1, 10)))
          .open();
    }
  }
}
