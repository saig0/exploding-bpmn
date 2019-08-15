package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;

public class DiscardHand implements JobHandler {

  private final Logger log;

  public DiscardHand(Logger log) {
    this.log = log;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {

  }
}
