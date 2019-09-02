package io.zeebe.bpmn.games.slack;

import io.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZeebeConfig {

  @Value("${zeebe.brokerContactPoint:localhost:26500}")
  private String brokerContactPoint;

  @Bean
  public ZeebeClient zeebeClient() {
    return ZeebeClient.newClientBuilder()
        .brokerContactPoint(brokerContactPoint)
        .usePlaintext()
        .build();
  }
}
