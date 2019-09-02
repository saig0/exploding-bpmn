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

  private static void blockKitMessage(String url) {
    ButtonElement button =
        ButtonElement.builder()
            .text(PlainTextObject.builder().emoji(true).text("Farmhouse").build())
            .value("click_me_123")
            .actionId("action-123")
            .build();

    LayoutBlock block = ActionsBlock.builder().elements(Arrays.asList(button)).build();

    List<LayoutBlock> blocks = Arrays.asList(block);

    Payload payload = Payload.builder().blocks(blocks).build();

    Slack slack = Slack.getInstance();
    try {
      WebhookResponse response = slack.send(url, payload);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void simpleMessage(String url) {
    Payload payload = Payload.builder().text("Hello World!").build();

    Slack slack = Slack.getInstance();
    try {
      WebhookResponse response = slack.send(url, payload);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // response.code, response.message, response.body
  }
}
