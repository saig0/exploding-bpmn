package io.zeebe.bpmn.games;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.zeebe.bpmn.games.model.Variables;

public class GameContext implements GameListener.Context {

  private final String key;

  private GameContext(String key) {
    this.key = key;
  }

  public static GameContext of(ActivatedJob job) {
    return new GameContext(Variables.from(job).getGameKey());
  }

  @Override
  public String getKey() {
    return key;
  }
}
