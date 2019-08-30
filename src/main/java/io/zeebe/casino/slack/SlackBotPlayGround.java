package io.zeebe.casino.slack;

import com.github.seratch.jslack.Slack;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.methods.response.chat.ChatPostMessageResponse;
import com.github.seratch.jslack.api.methods.response.dialog.DialogOpenResponse;
import com.github.seratch.jslack.api.model.block.ActionsBlock;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
import com.github.seratch.jslack.api.model.block.element.ButtonElement;
import com.github.seratch.jslack.api.model.dialog.Dialog;
import com.github.seratch.jslack.api.model.dialog.DialogOption;
import com.github.seratch.jslack.api.model.dialog.DialogSelectElement;
import com.github.seratch.jslack.api.model.dialog.DialogSubType;
import com.github.seratch.jslack.api.model.dialog.DialogTextElement;
import com.github.seratch.jslack.api.webhook.Payload;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.github.seratch.jslack.app_backend.interactive_messages.ResponseSender;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.AttachmentActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.PayloadTypeDetector;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayloadParser;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import com.github.seratch.jslack.app_backend.util.RequestTokenVerifier;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SlackBotPlayGround {

  private static final Logger LOG = LoggerFactory.getLogger(SlackBotPlayGround.class);
  // To extract the "type" property from the payload
  private final PayloadTypeDetector payloadTypeDetector = new PayloadTypeDetector();
  // To deserialize the payload JSON string to an object
  private final Gson gson = GsonFactory.createSnakeCase();
  private final String token = "???";
  private final String verificationToken = "???";
  // To make sure if the verification token is correct
  // When you go with the no arg constructor, the existence of env variable
  // "SLACK_VERIFICATION_TOKEN" would be expected.
  private RequestTokenVerifier tokenVerifier;
  // To send a webhook request to Slack Platform
  private ResponseSender responseSender;
  private Slack slack;

  public static void main(String... args) {
    SpringApplication.run(SlackBotPlayGround.class, args);
  }

  @RequestMapping(value = "/slack/action-123", method = RequestMethod.POST)
  public void greeting(@RequestParam Map<String, String> body) {
    LOG.info("Received {}", body);

    final var json = body.get("payload");

    String payloadType = payloadTypeDetector.detectType(json);
    if (AttachmentActionPayload.TYPE.equals(payloadType)) {
      AttachmentActionPayload payload = gson.fromJson(json, AttachmentActionPayload.class);
      if (!tokenVerifier.isValid(payload)) {

        return; // 403
      }
      // TODO: handle requests from some parts in attachments
      return; // 200

    } else if (BlockActionPayload.TYPE.equals(payloadType)) {
      BlockActionPayload payload = gson.fromJson(json, BlockActionPayload.class);
      if (!tokenVerifier.isValid(payload)) {
        return; // 403
      }
      for (BlockActionPayload.Action action : payload.getActions()) {
        // TODO: handle requests from some parts in attachments
        // If you have some heavy operations which may take a bit long, doing it asynchronously
        // would be preferable
        ActionResponse responseMessage =
            ActionResponse.builder()
                .responseType("ephemeral")
                .text(action.getValue() + " has been accepted. Thanks!") // or attachments / blocks
                .deleteOriginal(false)
                .replaceOriginal(false)
                .build();
        try {
          WebhookResponse apiResponse =
              responseSender.send(payload.getResponseUrl(), responseMessage);
          if (apiResponse.getCode() != 200) {
            LOG.error("Got an error from Slack Platform (response: {})", apiResponse.getBody());
            return; // 500
          }
        } catch (IOException e) {
          LOG.error(
              "Failed to send a response message to Slack Platform because {}", e.getMessage(), e);
          return; // 500
        }
      }

    } else {
      // "type" value is missing or an unexpected one
      return; // 400
    }
  }

  @RequestMapping(value = "/slack/new-game", method = RequestMethod.POST)
  public SlashCommandResponse newGame(@RequestBody String body) {
    LOG.info("Received {}", body);

    // final var text = body.get("text"); // @RequestParam Map<String, String> body

    final var slashCommandPayloadParser = new SlashCommandPayloadParser();
    final var payload = slashCommandPayloadParser.parse(body);

    final var text = payload.getText();

    LOG.info("Command text {}", text);

    final var players = new HashMap<String, String>();

    final var pattern = Pattern.compile("<@([0-9A-Z]+)\\|([\\w\\.\\-]+)>");

    final var playerNames = text.split("\\s|,");
    Arrays.stream(playerNames).forEach(name -> {

      final var matcher = pattern.matcher(name);
      if (matcher.find()) {
        final var userId = matcher.group(1);
        final var userName = matcher.group(2);

        LOG.info("Player id: {}, name: {}", userId, userName);

        players.put(userId, userName);
      }
    });

    SlashCommandResponse responseMessage =
        SlashCommandResponse.builder()
            .responseType("in_channel")
            .text(
                "Starting new game with "
                    + players.values().stream().collect(Collectors.joining(", ")))
            // or attachments / blocks
            .build();

    players
        .entrySet()
        .forEach(
            e -> {
              final var userId = e.getKey();
              final var userName = e.getValue();

              try {
                final var response = slack.methods(token).imOpen(r -> r.user(userId));

                final var channelId = response.getChannel().getId();

                LOG.info("send private message to {}", userName);

                // https://slack.com/api/chat.postMessage
                final var postResponse =
                    slack
                        .methods(token)
                        .chatPostMessage(req -> req.channel(channelId).text("Starting new game"));

              } catch (Exception ex) {
                throw new RuntimeException(ex);
              }
            });

    return responseMessage;
  }

  @PostConstruct
  public void init() {

    slack = Slack.getInstance();

    responseSender = new ResponseSender(Slack.getInstance());
    tokenVerifier = new RequestTokenVerifier(verificationToken);


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
