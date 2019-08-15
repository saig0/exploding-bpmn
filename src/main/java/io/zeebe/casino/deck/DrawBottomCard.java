package io.zeebe.casino.deck;

import static io.zeebe.casino.deck.DrawTopCard.drawCard;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Map;
import org.slf4j.Logger;

public class DrawBottomCard implements JobHandler {

  private final Logger log;

  public DrawBottomCard(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {
      final Map<String, Object> variables = drawCard(log, activatedJob, () -> Integer.MAX_VALUE);
      jobClient.newCompleteCommand(activatedJob.getKey()).variables(variables).send();
  }
}
