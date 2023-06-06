package io.zeebe.bpmn.games;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@Component
public class GameStarter {

  private static final Logger LOG = LoggerFactory.getLogger(GameStarter.class);

  private final ZeebeClient client;

  @Autowired
  public GameStarter(ZeebeClient client) {
    this.client = client;
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

    // {"playerNames":["bot_1", "bot_2"], "game-key": "key-1", "channelId": "channel-1"}

    return gameKey;
  }

}
