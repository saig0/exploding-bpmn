package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.Attachment;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayloadParser;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import io.zeebe.bpmn.games.GamesApplication;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack/command/")
public class SlashCommands {

  private static final Logger LOG = LoggerFactory.getLogger(SlashCommands.class);

  private final SlashCommandPayloadParser payloadParser = new SlashCommandPayloadParser();

  private final Pattern userPattern = Pattern.compile("<@([0-9A-Z]+)\\|([\\w\\.\\-]+)>");

  @Autowired private MethodsClient methodsClient;

  @Autowired private GamesApplication gamesApplication;

  @Autowired private SlackSession session;

  @PostMapping("/new-game")
  public SlashCommandResponse newGame(@RequestBody String body) {
    LOG.debug("Received new command 'new-game' with body {}", body);

    final var payload = payloadParser.parse(body);
    final var text = payload.getText();

    final List<String> userIds = getUserIds(text);

    if (userIds.size() < 2 || userIds.size() > 10) {
      return SlashCommandResponse.builder()
          .responseType("ephemeral")
          .text("You can play the game with 2 to 10 players. Let's try again.")
          .build();
    }

    LOG.debug("Start new game with players {}", userIds);

    userIds.forEach(this::openConversation);

    gamesApplication.startNewGame(userIds);

    final var playerList =
        userIds.stream()
            .map(userId -> String.format("<@%s>", userId))
            .collect(Collectors.joining(", "));

    return SlashCommandResponse.builder()
        .responseType("in_channel")
        .text(String.format("Starting new game with %s", playerList))
        .build();
  }

  private List<String> getUserIds(String text) {
    final var userIds = new ArrayList<String>();

    final var playerNames = text.split("\\s|,");
    Arrays.stream(playerNames)
        .forEach(
            name -> {
              final var matcher = userPattern.matcher(name);
              if (matcher.find()) {
                final var userId = matcher.group(1);
                final var userName = matcher.group(2);

                userIds.add(userId);
              }
            });

    return userIds;
  }

  private void openConversation(String userId) {
    try {

      final var response = methodsClient.imOpen(r -> r.user(userId));

      if (!response.isOk()) {
        throw new RuntimeException(
            "Fail to open channel to user: " + userId + ", caused by " + response.getError());
      }

    } catch (IOException | SlackApiException e) {
      throw new RuntimeException("Fail to open channel to user: " + userId, e);
    }
  }

  @PostMapping("/how-to-play")
  public SlashCommandResponse manual(@RequestBody String body) {
    LOG.debug("Received new command 'how-to-play' with body {}", body);

    final var payload = payloadParser.parse(body);

    final var imageUrl = "https://raw.githubusercontent.com/saig0/bpmn-games/slack-bot/games/src/main/resources/explodingKittens.PNG";

    return SlashCommandResponse.builder()
        .responseType("ephemeral")
        .text("The game is well documented as BPMN:")
        .attachments(List.of(Attachment.builder().imageUrl(imageUrl).build()))
        .build();
  }

}
