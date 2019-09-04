package io.zeebe.bpmn.games.action;

import io.zeebe.bpmn.games.model.Variables;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import java.util.Map;
import java.util.Optional;

public class ThrowMessage implements JobHandler {

  private final ZeebeClient client;

  public ThrowMessage(ZeebeClient client) {
    this.client = client;
  }

  @Override
  public void handle(JobClient jobClient, ActivatedJob job) {
    final var variables = Variables.from(job);

    final var correlationKey = variables.getCorrelationKey();

    final var messageName =
        Optional.ofNullable(job.getCustomHeaders().get("messageName"))
            .orElseThrow(() -> new RuntimeException("missing custom header 'messageName'"));

    final PublishMessageCommandStep3 publishMessageCommandStep3 =
        client.newPublishMessageCommand().messageName(messageName).correlationKey(correlationKey);

    Optional.ofNullable(job.getCustomHeaders().get("variable"))
        .ifPresent(
            variableName -> {
              final var value = job.getVariablesAsMap().get(variableName);
              publishMessageCommandStep3.variables(Map.of(variableName, value));
            });

    publishMessageCommandStep3.send().join();

    // don't complete job for interrupting message event
  }
}
