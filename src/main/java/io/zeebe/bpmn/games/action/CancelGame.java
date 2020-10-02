package io.zeebe.bpmn.games.action;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.GameContext;
import io.zeebe.bpmn.games.GameListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CancelGame implements JobHandler {

  private final GameListener listener;

  @Autowired
  public CancelGame(GameListener listener) {
    this.listener = listener;
  }

  @ZeebeWorker(type = "cancelGame")
  @Override
  public void handle(final JobClient client, final ActivatedJob job) throws Exception {

    listener.gameCanceled(GameContext.of(job));

    client.newCompleteCommand(job.getKey()).send().join();
  }
}
