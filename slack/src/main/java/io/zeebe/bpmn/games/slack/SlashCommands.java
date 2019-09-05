package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayloadParser;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import io.zeebe.bpmn.games.GamesApplication;
import io.zeebe.bpmn.games.slack.SlackContext.UserInfo;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack/command/")
public class SlashCommands {

  private static final Logger LOG = LoggerFactory.getLogger(SlashCommands.class);

  private final SlashCommandPayloadParser payloadParser = new SlashCommandPayloadParser();

  private final Pattern userPattern = Pattern.compile("<@([0-9A-Z]+)\\|([\\w\\.\\-]+)>");

  @Autowired private MethodsClient methodsClient;

  @Autowired private GamesApplication gamesApplication;

  @Autowired private SlackContext slackContext;

  @PostMapping("/new-game")
  public SlashCommandResponse newGame(@RequestBody String body) {
    LOG.debug("Received new command 'new-game' with body {}", body);

    final var payload = payloadParser.parse(body);
    final var text = payload.getText();

    final Map<String, String> players = getPlayerNames(text);

    if (players.size() < 2 || players.size() > 10) {
      return SlashCommandResponse.builder()
          .responseType("ephemeral")
          .text("You can play the game with 2 to 10 players. Let's try again.")
          .build();
    }

    LOG.debug("Start new game with players {}", players);

    players.entrySet().forEach(p -> sendPrivateMessage(p.getKey(), p.getValue()));

    final long key = gamesApplication.startNewGame(players.keySet());

    final var playerList =
        players.keySet().stream()
            .map(userId -> String.format("<@%s>", userId))
            .collect(Collectors.joining(", "));

    return SlashCommandResponse.builder()
        .responseType("in_channel")
        .text(String.format("Starting new game with %s", playerList))
        .build();
  }

  private HashMap<String, String> getPlayerNames(String text) {
    final var players = new HashMap<String, String>();

    final var playerNames = text.split("\\s|,");
    Arrays.stream(playerNames)
        .forEach(
            name -> {
              final var matcher = userPattern.matcher(name);
              if (matcher.find()) {
                final var userId = matcher.group(1);
                final var userName = matcher.group(2);

                players.put(userId, userName);
              }
            });

    return players;
  }

  private void sendPrivateMessage(String userId, String userName) {
    try {
      final var response = methodsClient.imOpen(r -> r.user(userId));

      final var channelId = response.getChannel().getId();

      slackContext.putUser(new UserInfo(userId, userName, channelId));

    } catch (SlackApiException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
