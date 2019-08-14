package io.zeebe.casino;

import io.zeebe.client.ZeebeClient;
import java.util.List;
import java.util.Map;

public class Application {

  public static void main(String[] args) {

    final var zeebeClient = ZeebeClient.newClientBuilder().usePlaintext().build();

    // ---
    System.out.println("> deploying workflows");
    zeebeClient.newDeployCommand().addResourceFromClasspath("explodingKittens.bpmn").send().join();

    // ---
    System.out.println("> start demo");
    zeebeClient
        .newCreateInstanceCommand()
        .bpmnProcessId("Process_1")
        .latestVersion()
        .variables(Map.of("players", List.of("phil", "chris")))
        .send()
        .join();
  }
}
