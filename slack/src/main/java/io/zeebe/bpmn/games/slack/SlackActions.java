package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.webhook.WebhookResponse;
import com.github.seratch.jslack.app_backend.interactive_messages.ResponseSender;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.AttachmentActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.BlockActionPayload;
import com.github.seratch.jslack.app_backend.interactive_messages.payload.PayloadTypeDetector;
import com.github.seratch.jslack.app_backend.interactive_messages.response.ActionResponse;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayloadParser;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import com.github.seratch.jslack.common.json.GsonFactory;
import com.google.gson.Gson;
import io.zeebe.bpmn.games.GamesApplication;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack/action/")
public class SlackActions {

  private static final Logger LOG = LoggerFactory.getLogger(SlackActions.class);

  private final PayloadTypeDetector payloadTypeDetector = new PayloadTypeDetector();
  private final Gson gson = GsonFactory.createSnakeCase();

  @Autowired
  private ResponseSender responseSender;


  @PostMapping("game")
  public void game(@RequestParam Map<String, String> body) {
    LOG.info("Received new action with body {}", body);

    final var json = body.get("payload");

    final var payloadType = payloadTypeDetector.detectType(json);
    if (AttachmentActionPayload.TYPE.equals(payloadType)) {
      final var payload = gson.fromJson(json, AttachmentActionPayload.class);

      // TODO: verify token -> 403
      // TODO: handle requests from some parts in attachments
      return; // 200

    } else if (BlockActionPayload.TYPE.equals(payloadType)) {
      final var payload = gson.fromJson(json, BlockActionPayload.class);
      // TODO: verify token -> 403
      for (BlockActionPayload.Action action : payload.getActions()) {
        // TODO: handle requests from some parts in attachments
        // If you have some heavy operations which may take a bit long, doing it asynchronously
        // would be preferable
        final var responseMessage =
            ActionResponse.builder()
                .responseType("ephemeral")
                .text(String.format("Action %s has been accepted.",  action.getValue()))
                .deleteOriginal(false)
                .replaceOriginal(false)
                .build();
        try {
          final var apiResponse =
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

}
