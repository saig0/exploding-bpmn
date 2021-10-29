package io.zeebe.bpmn.games;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.zeebe.bpmn.games.model.Variables;
import java.util.List;

public class GameContext implements GameListener.Context {

  private final String key;
  private final List<String> userIds;
  private final String channelId;

  private GameContext(String key, final List<String> userIds, final String channelId) {
    this.key = key;
    this.userIds = userIds;
    this.channelId = channelId;
  }

  public static GameContext of(ActivatedJob job) {
    final var variables = Variables.from(job);
    return new GameContext(
        variables.getGameKey(), variables.getPlayerNames(), variables.getChannelId());
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public List<String> getUserIds() {
    return userIds;
  }

  @Override
  public String getChannelId() {
    return channelId;
  }
}
