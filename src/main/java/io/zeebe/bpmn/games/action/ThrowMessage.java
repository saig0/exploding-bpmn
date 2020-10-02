package io.zeebe.bpmn.games.action;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.PublishMessageCommandStep1.PublishMessageCommandStep3;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.zeebe.bpmn.games.model.Variables;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ThrowMessage implements JobHandler {

  private final ZeebeClient client;

  @Autowired
  public ThrowMessage(ZeebeClient client) {
    this.client = client;
  }

  @ZeebeWorker(type = "throwMessage")
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
        .or(() -> Optional.ofNullable(job.getCustomHeaders().get("variables")))
        .map(variableNames -> variableNames.trim().split(","))
        .map(Arrays::asList)
        .ifPresent(
            variableNames -> {
              final var messageVariables =
                  variableNames.stream()
                      .collect(
                          Collectors.toMap(
                              Function.identity(),
                              variableName -> job.getVariablesAsMap().get(variableName)));
              publishMessageCommandStep3.variables(messageVariables);
            });

    publishMessageCommandStep3.send().join();

    // don't complete job for interrupting message event
  }
}
