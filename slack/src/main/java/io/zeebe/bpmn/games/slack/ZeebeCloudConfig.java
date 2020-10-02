package io.zeebe.bpmn.games.slack;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("cloud")
public class ZeebeCloudConfig {
  private static final Logger LOG = LoggerFactory.getLogger(ZeebeCloudConfig.class);

  @Value("${cloud.clientId}")
  private String cloudClientId;

  @Value("${cloud.clientSecret}")
  private String cloudClientSecret;

  @Value("${cloud.authServer:https://login.cloud.ultrawombat.com/oauth/token}")
  private String cloudAuthServer;

  @Value("${cloud.contactPoint}")
  private String contactPoint;

  @Bean
  public ZeebeClient zeebeClient() {
    LOG.info(
        "Configuration: \n Client id: {}, \n Client secret: {}, \n Auth server: {}, \n Contactpoint: {}",
        cloudClientId,
        cloudClientSecret,
        cloudAuthServer,
        contactPoint);

    final OAuthCredentialsProvider credentialsProvider =
        new OAuthCredentialsProviderBuilder()
            .audience(contactPoint)
            .clientId(cloudClientId)
            .clientSecret(cloudClientSecret)
            .authorizationServerUrl(cloudAuthServer)
            .build();

    return ZeebeClient.newClientBuilder()
        .brokerContactPoint(contactPoint + ":443")
        .credentialsProvider(credentialsProvider)
        .build();
  }
}
