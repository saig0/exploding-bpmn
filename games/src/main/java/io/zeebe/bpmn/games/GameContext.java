package io.zeebe.bpmn.games;

import io.zeebe.client.api.response.ActivatedJob;

public class GameContext implements GameListener.Context {

  private final long key;

  private GameContext(long key) {
    this.key = key;
  }

  public static GameContext of(ActivatedJob job) {
    return new GameContext(job.getWorkflowInstanceKey());
  }

  @Override
  public long getKey() {
    return key;
  }
}
