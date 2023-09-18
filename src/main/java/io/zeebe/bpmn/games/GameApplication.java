package io.zeebe.bpmn.games;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZeebeClient
@SpringBootApplication
@Deployment(resources = {"classpath*:/*.bpmn", "classpath*:/bot/*.dmn"})
public class GameApplication {

  public static void main(String[] args) {
    SpringApplication.run(GameApplication.class, args);
  }
}
