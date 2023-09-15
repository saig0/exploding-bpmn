package io.zeebe.bpmn.games.slack;

import com.github.seratch.jslack.api.methods.MethodsClient;
import com.github.seratch.jslack.api.methods.SlackApiException;
import com.github.seratch.jslack.api.model.block.LayoutBlock;
import com.github.seratch.jslack.api.model.block.SectionBlock;
import com.github.seratch.jslack.api.model.block.composition.MarkdownTextObject;
import com.github.seratch.jslack.app_backend.slash_commands.payload.SlashCommandPayloadParser;
import com.github.seratch.jslack.app_backend.slash_commands.response.SlashCommandResponse;
import io.zeebe.bpmn.games.GameApplication;
import io.zeebe.bpmn.games.GameStarter;
import io.zeebe.bpmn.games.model.CardType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/slack/command/")
@Profile("slack")
public class SlashCommands {

  public static final String BASE_URL =
      "https://raw.githubusercontent.com/saig0/bpmn-games/master/games/src/main/resources/";
  private static final Logger LOG = LoggerFactory.getLogger(SlashCommands.class);
  private static final String BPMN_IMAGE_URL = BASE_URL + "explodingKittens.png";
  private static final String BPMN_XML_URL = BASE_URL + "explodingKittens.bpmn";

  private final SlashCommandPayloadParser payloadParser = new SlashCommandPayloadParser();

  private final Pattern userPattern = Pattern.compile("<@([0-9A-Z]+)\\|([\\w\\.\\-]+)>");

  @Autowired private MethodsClient methodsClient;

  @Autowired private GameStarter gameStarter;

  @Autowired private SlackSession session;

  @GetMapping("/hello")
  public String helloSlackApp() {
    return "Hello user!";
  }

  @PostMapping("/new-game")
  public SlashCommandResponse newGame(@RequestBody String body) {
    LOG.debug("Received new command 'new-game' with body {}", body);

    final var payload = payloadParser.parse(body);
    final var text = payload.getText();
    final var channelId = payload.getChannelId();

    final List<String> userIds = getUserIds(text);

    if (userIds.size() < 2 || userIds.size() > 10) {
      return SlashCommandResponse.builder()
          .responseType("ephemeral")
          .text("You can play the game with 2 to 10 players. Let's try again.")
          .build();
    }

    LOG.debug("Start new game with players {}", userIds);

    userIds.stream().filter(userId -> !SlackUtil.isBot(userId)).forEach(this::openConversation);

    final String key = gameStarter.startNewGame(userIds, channelId);

    session.putGame(key, channelId, userIds);

    final var playerList =
        userIds.stream().map(SlackUtil::formatPlayer).collect(Collectors.joining(", "));

    return SlashCommandResponse.builder()
        .responseType("in_channel")
        .text(String.format("Starting new game :boom: Exploding BPMN :boom: with %s", playerList))
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
      final var response = methodsClient.conversationsOpen(r -> r.users(List.of(userId)));

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

    final var blocks = new ArrayList<LayoutBlock>();

    blocks.add(
        SectionBlock.builder()
            .text(
                MarkdownTextObject.builder()
                    .text(
                        ":boom: *Exploding BPMN* :boom: is like the game _Exploding Kittens_ but with BPMN symbols.")
                    .build())
            .build());

    blocks.add(
        SectionBlock.builder()
            .text(
                MarkdownTextObject.builder()
                    .text(
                        "The game is modeled in BPMN :bpmn: and executed in Camunda 8 SaaS :camunda-cloud: The code is available here: https://github.com/saig0/exploding-bpmn")
                    .build())
            .build());

    blocks.add(
        SectionBlock.builder()
            .text(
                MarkdownTextObject.builder()
                    .text(
                        "*Basic gameplay:*\n"
                            + "- every player starts with some hand cards\n"
                            + "- each turn, the current player can play as many cards from its hand as wanted or none\n"
                            + "- the turn ends when the player pass (i.e. plays no more card)\n"
                            + "- at the end of the turn, the player draws a new card\n"
                            + "- the game ends if a player draws an exploding card :boom: and doesn't have a defuse card left\n")
                    .build())
            .build());

    final BiFunction<CardType, String, String> format =
        (card, descr) -> String.format("- %s : %s\n", SlackUtil.formatCardType(card), descr);

    final TriFunction<CardType, CardType, String, String> formatPair =
        (card1, card2, descr) ->
            String.format(
                "- %s & %s : %s\n",
                SlackUtil.formatCardType(card1), SlackUtil.formatCardType(card2), descr);

    blocks.add(
        SectionBlock.builder()
            .text(
                MarkdownTextObject.builder()
                    .text(
                        "*Card descriptions:*\n\n"
                            + format.apply(CardType.EXPLODING, "game over")
                            + format.apply(CardType.DEFUSE, "save you once from exploding")
                            + format.apply(CardType.SEE_THE_FUTURE, "see the top 3 cards")
                            + format.apply(
                                CardType.ALTER_THE_FUTURE, "change the order of the top 3 cards")
                            + format.apply(
                                CardType.DRAW_FROM_BOTTOM,
                                "draw from the bottom (instead of the top)")
                            + format.apply(CardType.FAVOR, "get a card from a player")
                            + formatPair.apply(
                                CardType.CAT_1,
                                CardType.CAT_1,
                                "get a random card from a player (two cat cards of the same type)")
                            + formatPair.apply(
                                CardType.FERAL_CAT,
                                CardType.CAT_2,
                                "get a random card from a player (feral + a cat card)")
                            + format.apply(CardType.SHUFFLE, "shuffle the deck")
                            + format.apply(CardType.SKIP, "end your turn (without drawing a card)")
                            + format.apply(
                                CardType.ATTACK,
                                "end your turn and the next player has additional turns (your remaining turns + 2)")
                            + format.apply(
                                CardType.NOPE,
                                "invalid the last played card (the action is not applied) - can be chained to undo the nope")
                            + format.apply(CardType.ATOMIC,
                                "shuffle the deck and put all exploding kittens on top - end your turn (without drawing)"))
                    .build())
            .build());

    blocks.add(
            SectionBlock.builder()
                    .text(
                            MarkdownTextObject.builder()
                                    .text(
                                            "*Get started:*\n"
                                                    + "- type `/exploding-bpmn @player1 @player2 @player3` to start a new game\n"
                                                    + "- mention all players for the game, including you\n"
                                                    + "- add `bot_1` to invite a bot to the game (e.g. `/exploding-bpmn @player1 bot_1 bot_2`) :robot_face:\n"
                                                    + "- choose between a DMN-based bot `bot_1` and a unpredictable bot `bot_random_1`\n")
                                    .build())
                    .build());

    return SlashCommandResponse.builder().responseType("ephemeral").blocks(blocks).build();
  }

  @FunctionalInterface
  interface TriFunction<A, B, C, R> {
    R apply(A a, B b, C c);
  }
}
