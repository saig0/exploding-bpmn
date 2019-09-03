package io.zeebe.bpmn.games.slack;

import io.zeebe.bpmn.games.GameListener;
import io.zeebe.bpmn.games.GamesApplication;
import io.zeebe.bpmn.games.model.GameState;
import io.zeebe.bpmn.games.model.Player;
import io.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

  @Autowired private ZeebeClient zeebeClient;

  @Bean
  public GamesApplication games() {
    return new GamesApplication(zeebeClient, null);
  }
}
