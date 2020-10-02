package io.zeebe.bpmn.games;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZeebeClient
@SpringBootApplication
public class SlackApplication {

  public static void main(String... args) {
    SpringApplication.run(SlackApplication.class, args);
  }
}
