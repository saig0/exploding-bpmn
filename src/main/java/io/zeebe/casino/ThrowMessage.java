package io.zeebe.casino;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Optional;

public class ThrowMessage implements JobHandler {

  private final ZeebeClient client;

  public ThrowMessage(ZeebeClient client) {
    this.client = client;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final var correlationKey = (String) variables.get("correlationKey");

    final var messageName =
        Optional.ofNullable(activatedJob.getCustomHeaders().get("messageName"))
            .orElseThrow(() -> new RuntimeException("missing custom header 'messageName'"));

    client
        .newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .send()
        .join();

    // don't complete job for interrupting message event
  }
}
