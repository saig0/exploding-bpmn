package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.ImageBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.api.model.block.composition.PlainTextObject;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack/command/")
public class SlashCommands {

  public static final String BASE_URL =
      "https://raw.githubusercontent.com/saig0/bpmn-games/master/games/src/main/resources/";
  private static final Logger LOG = LoggerFactory.getLogger(SlashCommands.class);
  private static final String BPMN_IMAGE_URL = BASE_URL + "explodingKittens.png";
  private static final String BPMN_XML_URL = BASE_URL + "explodingKittens.bpmn";

  private final SlashCommandPayloadParser payloadParser = new SlashCommandPayloadParser();

  private final Pattern userPattern = Pattern.compile("<@([0-9A-Z]+)\\|([\\w\\.\\-]+)>");

  @Autowired private MethodsClient methodsClient;

  @Autowired private GamesApplication gamesApplication;

  @Autowired private SlackSession session;

  @GetMapping("/hello")
  public String helloSlackApp()
  {
    return "Hello user!";
  }

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

    userIds.stream().filter(userId -> !SlackUtil.isBot(userId)).forEach(this::openConversation);

    final var key = gamesApplication.startNewGame(userIds);

    session.putGame(key, payload.getChannelId(), userIds);

    final var playerList =
        userIds.stream()
            .map(SlackUtil::formatPlayer)
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

              } else if (SlackUtil.isBot(name)) {
                userIds.add(name);
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

    final var block1 =
        SectionBlock.builder()
            .text(
                MarkdownTextObject.builder()
                    .text("The game is well documented as BPMN (" + BPMN_XML_URL + ").")
                    .build())
            .build();

    final var block2 =
        ImageBlock.builder()
            .title(PlainTextObject.builder().text("Image").build())
            .imageUrl(BPMN_IMAGE_URL)
            .altText("the game as BPMN")
            .build();

    return SlashCommandResponse.builder()
        .responseType("ephemeral")
        .blocks(List.of(block1, block2))
        .build();
  }
}
