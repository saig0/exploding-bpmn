package io.zeebe.casino;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;

public class AddTurns implements JobHandler {

  public AddTurns(Logger log) {
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) throws Exception {

  }
}
