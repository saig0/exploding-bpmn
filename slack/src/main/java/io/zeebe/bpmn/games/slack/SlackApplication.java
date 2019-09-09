package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.element.ButtonElement;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import io.zeebe.bpmn.games.GamesApplication;
import io.zeebe.client.ZeebeClient;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
public class SlackApplication {

  private static final Logger LOG = LoggerFactory.getLogger(SlackApplication.class);

  @Autowired private ZeebeClient zeebeClient;
  @Autowired private GamesApplication gamesApp;

  public static void main(String... args) {
    SpringApplication.run(SlackApplication.class, args);
  }

  @PostConstruct
  public void init() {
    LOG.info("Checking connection to Zeebe...");
    zeebeClient.newTopologyRequest().send().join();
    LOG.info("Connected.");

    LOG.info("Launch Zeebe workers");
    gamesApp.start();

    LOG.info("Ready!");
  }

}
