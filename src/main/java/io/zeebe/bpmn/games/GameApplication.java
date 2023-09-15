package io.zeebe.bpmn.games;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZeebeClient
@SpringBootApplication
@ZeebeDeployment(resources = {"classpath*:/**/*.bpmn", "classpath*:/**/*.dmn"})
public class GameApplication {

  public static void main(String[] args) {
    SpringApplication.run(GameApplication.class, args);
  }
}
