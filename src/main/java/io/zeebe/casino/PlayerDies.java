package io.zeebe.casino;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.time.Duration;
import org.slf4j.Logger;

public class PlayerDies implements JobHandler {

  private final ZeebeClient zeebeClient;
  private final Logger log;

  public PlayerDies(ZeebeClient zeebeClient, Logger log) {
    this.zeebeClient = zeebeClient;
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final String correlationKey = activatedJob.getVariablesAsMap().get("correlationKey").toString();


    log.info("Send message ExplodingKitten with correlationKey {}.", correlationKey);
    zeebeClient
        .newPublishMessageCommand()
        .messageName("explodingKitten")
        .correlationKey(correlationKey)
        .timeToLive(Duration.ZERO)
        .send();
  }
}
