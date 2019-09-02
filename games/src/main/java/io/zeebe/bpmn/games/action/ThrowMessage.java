package io.zeebe.bpmn.games.action;

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
  public void handle(JobClient jobClient, ActivatedJob activatedJob) {
    final var variables = activatedJob.getVariablesAsMap();
    final var correlationKey = (String) variables.get("correlationKey");

    final var messageName =
        Optional.ofNullable(activatedJob.getCustomHeaders().get("messageName"))
            .orElseThrow(() -> new RuntimeException("missing custom header 'messageName'"));


    final PublishMessageCommandStep3 publishMessageCommandStep3 = client
        .newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey);

    final Optional<String> variableOptional = Optional
        .ofNullable(activatedJob.getCustomHeaders().get("variable"));
    if (variableOptional.isPresent())
    {
      final String variableName = variableOptional.get();
      final String assignee = activatedJob.getVariablesAsMap().get(variableName).toString();
      publishMessageCommandStep3.variables(Map.of("playerNoped", assignee));
    }

    publishMessageCommandStep3
        .send()
        .join();

    // don't complete job for interrupting message event
  }
}
