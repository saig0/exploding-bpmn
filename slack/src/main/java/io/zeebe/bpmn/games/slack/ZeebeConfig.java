package io.zeebe.bpmn.games.slack;

import io.zeebe.client.ZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class ZeebeConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeConfig.class);

  @Value("${zeebe.brokerContactPoint:localhost:26500}")
  private String contactPoint;

  @Bean
  public ZeebeClient zeebeClient() {
    LOG.info("Configuration: \n Contactpoint: {}", contactPoint);

    return ZeebeClient.newClientBuilder()
        .brokerContactPoint(contactPoint)
        .usePlaintext()
        .build();
  }
}
