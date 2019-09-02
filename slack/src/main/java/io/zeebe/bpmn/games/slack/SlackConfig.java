package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.app_backend.interactive_messages.ResponseSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SlackConfig {

  @Value("${slack.token}")
  private String token;

  @Bean
  public Slack slack() {
    return Slack.getInstance();
  }

  @Bean
  public MethodsClient methodsClient() {
    return slack().methods(token);
  }

  @Bean
  public ResponseSender responseSender() {
    return new ResponseSender(slack());
  }

}
